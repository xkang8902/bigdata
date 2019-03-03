/*
UDAF 
输入多行数据输出一行数据，一般在group by中使用。 
Evaluator需要实现 init、iterate、terminatePartial、merge、terminate这几个函数 
init初始化，iterate函数处理读入的行数据，terminatePartial返回iterate处理的中建结果，merge合并上述处理结果，terminate返回最终值。

解决问题描述:自己实现将相同主id下的子id用逗号拼接 

使用方法
add jar /home/mart_wzyf/zhuhongmei/plist_udf_udaf-0.0.1.jar;
CREATE TEMPORARY FUNCTION concat_sku_id AS 'com.jd.plist.udaf.TestUDAF';
select concat_sku_id(item_sku_id,',') from app.app_cate3_sku_info where dt =sysdate(-1)  and item_third_cate_cd = 870 group by main_sku_id;
DROP TEMPORARY FUNCTION concat_sku_id;
*/

import org.apache.hadoop.hive.ql.exec.UDAF;
import org.apache.hadoop.hive.ql.exec.UDAFEvaluator;
public class TestUDAF extends UDAF {
    public static class TestUDAFEvaluator implements UDAFEvaluator {
        public static class PartialResult {
            String skuids;
            String delimiter;
        }
 
        private PartialResult partial;
 
        public void init() {
            partial = null;
        }
 
        public boolean iterate(String item_sku_id, String deli) {
 
            if (item_sku_id == null) {
                return true;
            }
            if (partial == null) {
                partial = new PartialResult();
                partial.skuids = new String("");
                if (deli == null || deli.equals("")) {
                    partial.delimiter = new String(",");
                } else {
                    partial.delimiter = new String(deli);
                }
 
            }
            if (partial.skuids.length() > 0) {
                partial.skuids = partial.skuids.concat(partial.delimiter);
            }
 
            partial.skuids = partial.skuids.concat(item_sku_id);
 
            return true;
        }
 
        public PartialResult terminatePartial() {
            return partial;
        }
 
        public boolean merge(PartialResult other) {
            if (other == null) {
                return true;
            }
            if (partial == null) {
                partial = new PartialResult();
                partial.skuids = new String(other.skuids);
                partial.delimiter = new String(other.delimiter);
            } else {
                if (partial.skuids.length() > 0) {
                    partial.skuids = partial.skuids.concat(partial.delimiter);
                }
                partial.skuids = partial.skuids.concat(other.skuids);
            }
            return true;
        }
 
        public String terminate() {
            return new String(partial.skuids);
        }
 
    }
}

