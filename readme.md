## One SecKill Project

一个基于SpringBoot搭建的秒杀项目，整体框架基于Data Access Object (DAO) -> Service -> Controller结构设计（本项目来源于慕课网免费课程）

## 技术基础

- Spring Boot
- Maven
- MyBatis
- MySQL

## Tips
1. 为防止层与层之间的数据透传，每一层最好自己定义数据对象模型。
2. 数据库表设计过程中，在建表过程中，可以将用户信息与其对应的密码信息分开建表，可以在后续业务运行过程中提升查询效率，减少不必要的信息的查询，同时也是为了安全。


### 基础框架之Controller层设计

1. Controller层的统一异常处理，通过自定义一个异常处理类`BusinessException`实现，内部涉及一个异常信息接口`CommonError`以及一个异常信息枚举类`EmBusinessError`，处于`controller.error`包下。
2. Controller层的统一返回信息定义，通过自定义类`CommonReturnType`实现，处于`controller.response`包下。

### 业务模型之Service层设计

