FreqItem_Spark 中包含两个程序

# AprioriOnSpark
算法通过将原有的apriori算法改进为相应的分布式系统的apriori，考虑到数据量非常大的情况下我们将数据库中的所有事物读取为RDD，通过每一次将AprioriGen函数连接得到的候选项集Ck广播到集群中更新Ck出现在事物集中的数目。然后collect到client上进行下一次迭代的剪枝连接。考虑到算法中剪枝和连接步骤在支持度较大的时候数量较少，所以我们选择将剪枝和连接放在client进行处理，只将原始事物集数据放在集群中进行对候选集的扫描计数。



AprioriSeq  是带序列模式的Apriori  的分布式实现

//最原始根据账号group之后组成的列表是：
//List(E, C, A)
//List(C, D, C, A)
//List(D, E)
//List(A, B, C, D)
//List(A, B, C, D, E)
//db count:5 minSupport:2 Lk count: 5

//Round: 1
//(List(E),3)
//(List(D),4)
//(List(B),2)
//(List(A),4)
//(List(C),5)
//Round: 2
//(List(C, A),2)
//(List(C, D),3)
//(List(A, B),2)
//(List(D, E),2)
//(List(B, C),2)
//Round: 3
//(List(A, B, C),2)
//(List(B, C, D),2)
//Round: 4
//(List(A, B, C, D),2)
//Lk_last: 
//List(A, B, C, D)

//注意本程序是严格按照直接连接关系才算是频繁项，比如 A, B, C  中， A, B是频繁项，B, A 和 A, C 都不是频繁项。      
//更广义的情况下，如果要考虑A, C 也是频繁项的话，程序又要改一改


可以用Freq_prepare/gen_apriseq_ori.py  转换生成需要的输入文件然后作为AprioriSeq 的输入

