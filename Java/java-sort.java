public class SortUtils {
    //二分查找：
    public static int binarySearch(int a[], int value, int n) {
        int low, high, mid;
        low = 0;
        high = n-1;
        while(low <= high) {
            mid = (low+high)/2;
            if(a[mid] == value)
                return mid;
            if(a[mid] > value)
                high = mid-1;
            if(a[mid] < value)
                low = mid+1;
        }
        return -1;
    }

    //冒泡排序：
    public static void bubbleSort(int[] arr){
        for (int i = 0; i < arr.length; i++) {
            for (int j = 1; j < arr.length-i; j++) {
                int tmp = 0;
                if (arr[j-1] > arr[j]) {
                    tmp = arr[j];
                    arr[j] = arr[j-1];
                    arr[j-1] = tmp;
                }
            }
        }
    }

    //快速排序：
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            // 找寻基准数据的正确索引
            int index = getIndex(arr, low, high);
            // 进行迭代对index之前和之后的数组进行相同的操作使整个数组变成有序
            quickSort(arr, low, index - 1);
            quickSort(arr, index + 1, high);
        }
    }
    private static int getIndex(int[] arr, int low, int high) {
        // 基准数据
        int tmp = arr[low];
        while (low < high) {
            // 当队尾的元素大于等于基准数据时,向前挪动high指针
            while (low < high && arr[high] >= tmp) {
                high--;
            }
            // 如果队尾元素小于tmp了,需要将其赋值给low
            arr[low] = arr[high];
            // 当队首元素小于等于tmp时,向前挪动low指针
            while (low < high && arr[low] <= tmp) {
                low++;
            }
            // 当队首元素大于tmp时,需要将其赋值给high
            arr[high] = arr[low];
        }
        // 跳出循环时low和high相等,此时的low或high就是tmp的正确索引位置
        // 由原理部分可以很清楚的知道low位置的值并不是tmp,所以需要将tmp赋值给arr[low]
        arr[low] = tmp;
        return low; // 返回tmp的正确位置
    }

    //归并排序：
    // 归并排序，非递归实现(迭代)
    public static void sortMerge(int[] nums) {
        int len = 1;  // 初始排序数组的长度
        while(len < nums.length) {
            for(int i = 0; i < nums.length; i += len * 2) {
                sortMergeHelper(nums, i, len);
            }
            len *= 2;  // 每次将排序数组的长度*2
        }
    }
    private static void sortMergeHelper(int[] nums, int start, int len) {
        int[] tem = new int[len * 2];
        int i = start;
        int j = start + len;
        int k = 0;
        //有i 有j的情况
        while(i < start + len && (j < start + len + len && j < nums.length)) {
            tem[k++] = nums[i] < nums[j] ? nums[i++] : nums[j++];
        }   
        //只有i 没有j的情况  即不符合前一个while循环。
        while(i < start + len && i < nums.length) {  // 注意：这里i也可能超出长度
            tem[k++] = nums[i++];
        }
        /*    什么情况 ？
            while(j < start + len + len && j < nums.length) {
                tem[k++] = nums[j++];
            }
        */
        int right = start + len + len;
        int index = 0;
        while(start < nums.length && start < right) {
            nums[start++] = tem[index++];
        }
    }
}
//单例模式
public class InstanceSinglton{

    private static volatile InstanceSinglton instance;
    //other fields ....

    private InstanceSinglton(){}
    
    public static getInstance(){
        if(instance == null){
            synchronized(InstanceSinglton.class){
                if(instance == null){
                    instance = new InstanceSinglton();
                }
            }
        }
        return instance;
    }

    //other methods ....

}
//链表增删改查：
public class MyList{

    private class Node {
        public int data;
        public Node next;
        
        public Node(){}
        public Node(int data){
            this.data = data;
        }
    } 

    public MyList(){}
    public Node head;

    //获取长度
    public int getLongth(){
        int count = 0;
        if(head == null) {return count;}
        Node cur = head;
        while(cur != null){
            count++;
            cur = cur.next;
        }
        return count;
    }

    //增  尾插法
    public void add(int data){
        Node newNode = new Node(data);
        if(head == null){
            head = newNode;
            return;
        }
        Node cur = head;
        while(cur != null){
            cur = cur.next;
        }
        cur = newNode;       
    }

    //删  删除第几个节点
    public boolean del(int index){

        if(head == null || num <= 0 || num > getLongth()) return false;

        int count = 0;
        Node cur = head;
        Node pre = null;

        while(cur != null && count != index){
            count++;
            pre = cur;
            cur = cur.next;
        }

        pre = cur.next;
        cur.next = null;
        return true;
    }

    //改 查  逻辑相同
    public int query(int index){

        if(head == null || index <= 0 || index >= getLength()) throw exception("异常!");
        
        int count = 0;
        Node cur = head;

        while(cur != null && count != index){
            count++;
            cur = cur.next;
        }

        return cur.data;
    }

    //翻转    
    public Node reverse() {

        Node pre = null;
        Node next = null;
        Node node = head;

        while (node != null) {
            next = node.next;

            node.next = pre;
            pre = node;

            node = next;
        }

        return pre;
    }


}



