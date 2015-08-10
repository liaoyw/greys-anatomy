已经迁移到Github

https://github.com/oldmanpushcart/greys-anatomy


用于Java应用程序诊断的一个小工具，只能用在运行于Java6+的应用程序上
命令说明
目前为了简单，我将脚本简单的写成了只支持linux环境的情况

**安装
  * curl -sLk http://greys-anatomy.googlecode.com/files/install.sh|bash** 执行环境
    1. Greys Anatomy是建立在Java6新增的agent功能上的，所以对系统的最低要求必须是Java6+的运行环境
    1. 必须保证拥有足够的权限植入ga的agent功能
**使用方式
> > ` ./ga <pid> [port] `** 常见命令
  * monitor
    * monitor命令，能对指定的类+方法埋入探点，用以统计指定周期之内的执行情况
    * 参数说明
      * -c：需要埋入探点的类名正则表达式
      * -m：需要埋入探点的方法名正则表达式
      * -cy：输出周期，单位s
    * 输出说明
      1. 时间戳
      1. 类名
      1. 方法名
      1. 总调用量
      1. 成功次数
      1. 失败次数
      1. RT
      1. 失败率
  * profiler
    * profiler命令，能对指定的类+方法埋入探点，用以统计、跟踪调用路径
      * -c：需要埋入探点的类名正则表达式
      * -m：需要埋入探点的方法名正则表达式
      * -ec：开始跟踪的类名正则表达式
      * -em：开始跟踪的方法名正则表达式
      * -cl：跟踪条件，超时条件，单位ms
  * detail-class
    * detail-class命令，能详细的列出匹配上的类信息
      * -c：需要展示的类名正则表达式
      * -s：父类或接口名的正则表达式
  * detail-method
    * detail-method命令，能详细的列出匹配上的方法信息
      * -c：需要展示的方法所在类的类名正则表达式
      * -m：需要展示的方法名正则表达式
  * jobs
    * jobs命令，script、monitor、profiler这几个Probe类的命令都是以jobs的形式运行在agent中的，该命令能列出当前正在运行的job-id
  * kill
    * kill命令，能将立即杀死指定的job-id
      * -id：指定要杀死的job-id，如果啥都不填，则默认杀死所有job
  * script
    * script这个是我最喜欢的一个指令，能让ga加载一个javascript脚本进入探点，这能让你通过script脚本做很多和btrace差不多的事情
      * -c：需要埋入探点的类名正则表达式
      * -m：需要埋入探点的方法名正则表达式
      * -sf：执行的脚本路径+名称
      * 例子：
> > 命令
```
script -c=com\.mysql\.jdbc\.PreparedStatement -m=executeQuery -sf=/tmp/js/w.js
```
> > /tmp/js/w.js
```
function before(p,output,tls)
{
	output.println($f(p.targetThis,'originalSql'));
}
```
  * 关于js的详细说明
> > js中可以实现4个方法，分别是：
```
function before(p,output,tls);
function success(p,output,tls);
function exception(p,output,tls);
function finished(p,output,tls);
```
    * 参数p的具体信息可以参考 com.googlecode.greysanatomy.probe.Probe
    * 参数output其实只有一个方法，将字符串信息输出到console端
```
public void println(String msg);
```
    * 参数tls内部采用ThreadLocal实现，所以你可以通过tls参数来进行线程隔离，之所以起名tls则是参考了Btrace的@TLS
```
public void put(String name, Object value);
public Object get(String name);
```
    * 由于script中只能取到对象可见的成员，如果想取得不可见的成员，则需要采用特殊的内部函数
```
/**
* @obj 目标对象
* @fieldname 属性名称信息 '字符串'
*/
function $f(obj,fieldname);
```