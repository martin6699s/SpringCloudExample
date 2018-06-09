一、安装redis

1. 下载redis[安装包](https://redis.io/download), macOS系统下不要用brew install redis安装，无法使用集群，我安装的是redis-4.9.0版
2. 安装redis包，奉上[官方指引](https://redis.io/download#installation)

​      我把它解压到了/usr/local/Cellar/

```bash
sudo tar -zxvf redis-4.0.9.tar.gz -C /usr/local/Cellar/
```

​    进入redis压缩目录，并执行二进制安装

  ```shell
cd /usr/local/Cellar/redis-4.0.9
make
  ```

[可参考更详细Redis集群教程](http://www.redis.cn/topics/cluster-tutorial.html)

二、配置redis配置文件

1. 在/usr/local/etc/建redis-cluster目录，里面分别建7001目录、7002目录、7003目录、7004目录、7005目录、7006目录

```shell
cd /usr/local/etc/
mkdir redis-cluster
cd redis-cluster
mkdir 7001 7002 7003 7004 7005 7006

```

2. 把redis.conf复制到每个700*目录下

   ```shell
   cp /usr/local/Cellar/redis-4.0.9/redis.conf /usr/local/etc/redis-cluster/7001/redis-7001.conf
   cp /usr/local/Cellar/redis-4.0.9/redis.conf /usr/local/etc/redis-cluster/7002/redis-7002.conf
   cp /usr/local/Cellar/redis-4.0.9/redis.conf /usr/local/etc/redis-cluster/7003/redis-7003.conf
   cp /usr/local/Cellar/redis-4.0.9/redis.conf /usr/local/etc/redis-cluster/7004/redis-7004.conf
   cp /usr/local/Cellar/redis-4.0.9/redis.conf /usr/local/etc/redis-cluster/7005/redis-7005.conf
   cp /usr/local/Cellar/redis-4.0.9/redis.conf /usr/local/etc/redis-cluster/7006/redis-7006.conf
   ```

3. 需要修改每个redis-700*.conf配置文件，修改内容如下：

   ```redis.conf
   #统一配置，将对应的配置项注解掉，提前到此处
   ​```
   #All the config is move to here
   
   #redis后台运行
   daemonize yes
   
   #端口7001，7002,7003
   port 7001
   
   #数据文件存放位置
   dir /usr/local/etc/redis-cluster/7001/data/
   
   #集群的配置配置文件首次启动自动生成 7000,7001,7002
   cluster-config-file nodes_7001.conf
   
   #aof日志文件名
   appendfilename "appendonly-7001.aof"
   
   #pidfile文件对应7000,7001,7002,7003
   pidfile /var/run/redis_7001.pid
   
   #开启集群 把注释#去掉
   cluster-enabled yes
   
   #请求超时 设置5秒够了
   cluster-node-timeout 5000
   
   #aof日志开启有需要就开启，它会每次写操作都记录一条日志
   appendonly yes
   
   #绑定ip,如果想要远程登录，就将其注释掉
   #bind 127.0.0.1
   
   #登陆密码 完成集群前不要设置，否则无法进行主从节点的链接
   #requirepass passwd
   #masterauth passwd
   #是否开启保护模式，如果想要远程操作（非本机操作）就将其设为no，否则为yes
   protected-mode no
   ```

   

4. redis集群 mac需要安装ruby环境，要求在2.2.2以上，我安装2.5.0

   ```shell
   # macOs
   brew install ruby
   # Centos
   yum install ruby
   yum install rubygems
   
   
   # 安装后可能软链接失败，如Error: The `brew link` step did not complete successfully。需要对/usr/local/share/emacs/site-lisp设置目录拥有者为自己的用户
   sudo chown 用户 -R /usr/local/share/emacs/site-lisp
   # 再次创建软链接
    brew link ruby
   # 把ruby的bin目录加入PATH
   echo 'export PATH="/usr/local/Cellar/ruby/2.5.1/bin:$PATH"' >> ~/.zshrc
   ```


   5. Ruby的redis接口没有安装 ,  需要安装ruby的redis接口。

   ```shell
   ➜  ~ gem install redis
   Fetching: redis-4.0.1.gem (100%)
   Successfully installed redis-4.0.1
   Parsing documentation for redis-4.0.1
   Installing ri documentation for redis-4.0.1
   Done installing documentation for redis after 0 seconds
   1 gem installed
   ```

5. 如果没安装Ruby redis接口，在设置集群时，会报错如下：

   ```shell
   ➜  src ./redis-trib.rb create --replicas 1 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 127.0.0.1:7006
   Traceback (most recent call last):
   	2: from ./redis-trib.rb:25:in `<main>'
   	1: from /usr/local/Cellar/ruby/2.5.1/lib/ruby/2.5.0/rubygems/core_ext/kernel_require.rb:59:in `require'
   /usr/local/Cellar/ruby/2.5.1/lib/ruby/2.5.0/rubygems/core_ext/kernel_require.rb:59:in `require': cannot load such file -- redis (LoadError)
   ```

   

三、启动redis集群服务

1.  启动redis服务。

   这时候还不能算集群，因为暂时还并不在一个集群中，互相直接发现不了，而且还没有可存储的位置，就是所谓的**slot（槽）**。

```
redis-server /usr/local/etc/redis-cluster/7001/redis-7001.conf
redis-server /usr/local/etc/redis-cluster/7002/redis-7002.conf
redis-server /usr/local/etc/redis-cluster/7003/redis-7003.conf
redis-server /usr/local/etc/redis-cluster/7004/redis-7004.conf
redis-server /usr/local/etc/redis-cluster/7005/redis-7005.conf
redis-server /usr/local/etc/redis-cluster/7006/redis-7006.conf
```

2. 进行主从节点的链接，真正启动集群

```shell
cd /usr/local/Cellar/redis-4.0.9/src &&
./redis-trib.rb create --replicas 1 127.0.0.1:7001 127.0.0.1:7002 127.0.0.1:7003 127.0.0.1:7004 127.0.0.1:7005 127.0.0.1:7006
# 注意：--replicas  1 代表每个master有一个slave，还有就是前面三个是主服务，后面三个从服务地址，集群配置成功。

# 若出现以下提示，表示操作成功
>>> Creating cluster
>>> Performing hash slots allocation on 6 nodes...
Using 3 masters:
127.0.0.1:7001
127.0.0.1:7002
127.0.0.1:7003
Adding replica 127.0.0.1:7005 to 127.0.0.1:7001
Adding replica 127.0.0.1:7006 to 127.0.0.1:7002
Adding replica 127.0.0.1:7004 to 127.0.0.1:7003
>>> Trying to optimize slaves allocation for anti-affinity
[WARNING] Some slaves are in the same host as their master
M: 74cedb9f95fa9208c705b164fe7ed246448f8a9a 127.0.0.1:7001
   slots:0-5460 (5461 slots) master
M: e6c35f3a4e449a864392ced0744a772a71d50473 127.0.0.1:7002
   slots:5461-10922 (5462 slots) master
M: e2038eb1e65f3a0e54232afdc2c09c8074f379fc 127.0.0.1:7003
   slots:10923-16383 (5461 slots) master
S: 5d7f1ddaceb33060be7ec40806bdcd864d6d9d97 127.0.0.1:7004
   replicates e6c35f3a4e449a864392ced0744a772a71d50473
S: 601adfe20474b4b9f4be753408e66f8a7623da08 127.0.0.1:7005
   replicates e2038eb1e65f3a0e54232afdc2c09c8074f379fc
S: ff5442878c2dedffed9d334e6aa1864fd4cc23bd 127.0.0.1:7006
   replicates 74cedb9f95fa9208c705b164fe7ed246448f8a9a
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join......
>>> Performing Cluster Check (using node 127.0.0.1:7001)
M: 74cedb9f95fa9208c705b164fe7ed246448f8a9a 127.0.0.1:7001
   slots:0-5460 (5461 slots) master
   1 additional replica(s)
S: 5d7f1ddaceb33060be7ec40806bdcd864d6d9d97 127.0.0.1:7004
   slots: (0 slots) slave
   replicates e6c35f3a4e449a864392ced0744a772a71d50473
M: e6c35f3a4e449a864392ced0744a772a71d50473 127.0.0.1:7002
   slots:5461-10922 (5462 slots) master
   1 additional replica(s)
S: 601adfe20474b4b9f4be753408e66f8a7623da08 127.0.0.1:7005
   slots: (0 slots) slave
   replicates e2038eb1e65f3a0e54232afdc2c09c8074f379fc
M: e2038eb1e65f3a0e54232afdc2c09c8074f379fc 127.0.0.1:7003
   slots:10923-16383 (5461 slots) master
   1 additional replica(s)
S: ff5442878c2dedffed9d334e6aa1864fd4cc23bd 127.0.0.1:7006
   slots: (0 slots) slave
   replicates 74cedb9f95fa9208c705b164fe7ed246448f8a9a
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.

```

3.  远程访问控制 并测试

```shell
#本机登陆redis进行测试,其中-c表示使用redis命令模式下启动集群，这样不会报“(error) MOVED ... ”会帮我们自动切换到对应的节点上

➜  src redis-cli -c -h 127.0.0.1 -p 7001
127.0.0.1:7001> set goods "231211"
OK
127.0.0.1:7001> get goods
"231211"
127.0.0.1:7001> set name dsds
-> Redirected to slot [5798] located at 127.0.0.1:7002
OK
127.0.0.1:7002> get name
"dsds"
127.0.0.1:7002>
```

   

