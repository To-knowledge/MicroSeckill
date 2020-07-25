## One SecKill Project

一个基于SpringBoot搭建的秒杀项目:rocket:，整体框架基于Data Access Object (DAO) -> Service -> Controller结构设计（本项目来源于慕课网免费课程）

## 技术基础

- Spring Boot
- Maven
- MyBatis
- MySQL
- IDEA

## Tips
1. 为防止层与层之间的数据透传，每一层最好自己定义数据对象模型。
2. 数据库表设计过程中，在建表过程中，可以将用户信息与其对应的密码信息分开建表，可以在后续业务运行过程中提升查询效率，减少不必要的信息的查询，同时也是为了安全。


### 基础框架之Controller层设计

1. Controller层的统一异常处理，通过自定义一个异常处理类`BusinessException`实现，内部涉及一个异常信息接口`CommonError`以及一个异常信息枚举类`EmBusinessError`，处于`controller.error`包下。
2. Controller层的统一返回信息定义，通过自定义类`CommonReturnType`实现，处于`controller.response`包下。

### 业务模型之Service层设计

#### 用户模型管理
##### 1.1 注册短信验证码获取
通过定义getOpt方法处理前端提交的请求，该方法需要处理的逻辑如下

```java
public ReturnType getOpt(@RequestParam(name="telephone") String telephone){
	//按照一定规则生成验证码
	//将Opt验证码同用户手机号关联，一般采用Redis，此处先采用HttpSession实现
	httpServeletRequest.getSession.setAttribute(telephone, optCode);
	//将验证码通过短信通道发送给用户
}
```

此处的HttpSession可以通过自动装载`HttpServletRequest`（单例）得到，此处之所以可以存储多个用户信息，是由于其内部采用Threalocal实现多线程绑定，使得每个用户的先短信验证信息隔离。
##### 1.2 前端页面简易处理
通过javascript响应事件，后端收到post请求时，调动getOpt方法进行处理。此处针对后端代码需要做如下处理：
1. 完善RequestMapping注解的参数；
```java
@RequestMapping(value = "/getOtp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
```
2. 加入跨域请求注解`@CrossOrigin`，加到`UserController`类上，加载到方法getOpt上会依然报错（后来被验证无效，在方法上加与不加一样），但是可以与后端产生交互；
```java
//不设置参数的话，依然无法避免
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
```
##### 2.1 注册功能实现
```java
public ReturnType register(//@RequestParam获取前端传入的数据){
	// 1.验证用户的手机号与验证码符合
	// 2.进入注册流程，分层设计
	// 3.返回注册成功信息
}
```
用户自增id不需要传入值，非自增字段，且没有设置默认值会导致mybatis报错。

为避免相同手机号多次注册，可以将telephone字段设为唯一索引。

##### 3.1 登录功能实现
主要涉及通过用户手机号获取用户信息，需要在UserDOMapper.xml中加入相关的数据库查询代码，如下：
```xml
<select id="selectByTelephone" resultMap="BaseResultMap">
select
<include refid="Base_Column_List"/>
from user_info
where telephone = #{telephone, jdbcType=VARCHAR}
</select>
```
然后，在UserDO.java中加入对应的方法：
```java
UserDO selectByTelephone(String telephone);
```
验证成功后，将登录凭证加入到用户登录成功的session内。
#### 校验规则优化
1. 调用Maven仓库中的Hibernate Validator Engine中的模块；
2. 通过@NotBlank、@NotNull、@Max、@Min注解约束参数的范围

#### 商品模型设计
优先设计领域模型（不能一上来就去数据库建表）
领域模型
```java
public class ItemModel{
	// id字段
	private Integer id;
	//商品名称
	private String title;
	//价格
	private BigDecimal price;
	//库存
	private Integer stock;
	//商品描述
	private String description;
	//商品销量
	private Integer sales;
	//商品图片url
	private String url;
}
```
根据领域模型合理分表，创建对应表结构，最后通过mybatis-generator生成DOMapper.xml、DOMapper的java接口、DO类。

此处分出了两张表，一张商品信息(item)表，一张库存(item_stock)表，~~修改DOMopper.xml使得item的id与item_stock中的item_id关联。~~（删除线中的内容对于这一关联操作，是通过java代码实现的，先取出item的id，然后把它赋值给item_stock表的item_id字段）。
```xml
(ItemDOMapper.xml以及ItemStockMapper.xml)
(找到insert操作以及insertSelective操作) userGenertedKeys = "true" keyProperty = "id"(不是true) (此处的原因与id自增有关，既然如此，为何myBatis在设计的时候为啥不默认为true)(后来发现，如果不写，service层通过DO取不出来id值)
```
##### 商品的service层接口
主要需要有创建商品、商品列表浏览、商品详情浏览等功能。
```java
ItemModel createItem(ItemModel itemModel);
List<ItemModel> listItem();
ItemModel getItemById(Integer id);
```
创建Item的方法设计流程：
```java
@Transactional 
public ItemModel createItem(ItemModel itemModel){
// 1.校验入参，通过先前定义的ValdationImpl实现
// 2.转化itemModel -> dataObject
// 3.写入数据库
// 4.返回创建完成的对象
// 注记：double在传到前端会有精度损失的问题，需要转化为BigDecmal
}
```
