## ArrayList

ArrayList是基于object数组实现的，默认大小为10，每次添加数据时检查数组大小，保证不溢出；若满了，每次调整为原来长度的1.5倍，然后使用Arrays.copyOf将原数组拷贝到新数组;

ArrayList线程不安全；

ArrayList删除数据后，后面的元素都要前移；

## LinkedList

LinkedList是基于双向链表实现；

first,last首尾节点；每个节点都有前驱和后继节点；


## 使用场景

1. 如果对索引位置数据进行大量存取和删除操作，使用ArrayList要好于LinkedList；

2. 如果是循环迭代，lInkedList要由于ArrayList;

ArrayList适用于频繁查询和获取数据；LinkedList适合频繁的增加和删除数据；