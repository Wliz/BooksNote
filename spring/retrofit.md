# Retrofit

## 背景简介
1. Retrofit是一个基于okhttp的网络请求框架；
2. 通过注解配置网络请求参数；
3. 支持图片链接和图片上传；
4. 支持异步和同步网络请求；
5. 支持多种数据的解析，提供对RxJava的支持；
6. 可扩展性好，高度封装，简洁易用；

[Retrofit 官网](https://square.github.io/retrofit/])

## 注解使用
### 方法注解
方法注解支持restful请求方式，如下：
- @GET
- @POST
- @PUT
- @DELETE
- @OPTIONS
- @PATCH

### 方法注解搭配
- @FormUrlEncoded: 与@POST，@Field搭配使用；
```Java
// 请求头：Content-Type: application/x-www-form-urlencoded
// 路径参数(且路径会进行rul编码)：{host:port}/abc/a?id=xxx&name=xxx
@POST("/abc/a")
@FormUrlEncoded
CallResponse<User> updateUser(@Field("id") Integer id, @Field("name") String name);

```
- @Multipart,@Part注解与@POST注解搭配使用；(form表单传递)
```Java
// 请求头：Content-Type: multipart/form-data
// 路径参数：{host:port}/abc/a
// 请求参数：form表单，id，img（这里指图片）
@POST("/abc/a")
@Multipart
CallResponse<User> updateUser(@Part("id") Integer id, @Part("img") RequestBody img)
```
- @Streaming: 使用流的方式接收返回值
- @Headers: 请求头固定值，与@Header，@HeaderMap可搭配
- @Path：标志参数替换请求路径
```Java
// 请求头：Accept,User-Agent
// 请求路径：{host:port}/users/xxxx
@Headers({
    "Accept: application/vnd.github.v3.full+json",
    "User-Agent: Retrofit-Sample-App"
})
@GET("users/{username}")
Call<User> getUser(@Path("username") String username);
```
- @Url: 用来替换请求路径，方便动态路径生成的；不能与@Path注解同时使用
```Java
// 当不想在请求方式后添加路径时，可以使用@Url作为参数进行动态替换
@GET
Call<ResponseBody> list(@Url String url);
```