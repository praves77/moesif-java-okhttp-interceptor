# Moesif Java OkHttp Interceptor SDK  
  
  
## Introduction  
  
`moesif-java-okhttp-interceptor` is a Java OkHttp interceptor that logs outbound HTTP(s) calls and sends events to [Moesif](https://www.moesif.com) for API analytics and monitoring    .  
  
The SDK includes `Java` and `Kotlin` examples. It is implemented as a [OkHttp Interceptor](https://square.github.io/okhttp/interceptors/)  
and can be used either as `Application Interceptor ` or `Network Interceptor`. It requires `Moesif Application Id` credentials to submit events to Moesif.  
  
With a single statement `.addInterceptor(new MoesifOkHttp3Interceptor())` it can start capturing events.  
  
[Source Code and samples on GitHub](https://github.com/moesif/moesif-play-filter) //todo update URL  
  
## How to install  
For Maven users, add dependency to your `pom.xml`: //todo need to update  
  
```xml  
<dependency>  
 <groupId>com.moesif.filter</groupId> <artifactId>moesif-play-filter</artifactId> <version>1.1</version></dependency>  
```  
//todo need to update  
For Gradle users, add the Moesif dependency to your project's build.gradle file:  
  
```gradle  
// Include jcenter repository if you don't already have it.  
repositories {  
 jcenter()}  
  dependencies {     
    compile 'com.moesif.filter:moesif-play-filter:1.1'  
}  
```  
  
#### Others  
//todo  
  
The jars are available from a public [Bintray Jcenter](https://bintray.com/moesif/maven/moesif-servlet) repository.  
  
  
## How to use  
Set the Moesif Application Id environment variable. Alternatively, this key can also be directly passed using `MoesifApiConnConfig`.  
  
```bash  
$ export MOESIF_APPLICATION_ID = "Your Moesif App Id Here"  
```  
Build the OkHttp client.  
  
The Moesif OkHttp Interceptor can be utilized for both types of [OkHttp interceptors Link](https://square.github.io/okhttp/interceptors/)  
  
| |  Application Interceptor |  
|---|---|  
| Java  | `addInterceptor(new MoesifOkHttp3Interceptor())`  |
| Kotlin |`addInterceptor(MoesifOkHttp3Interceptor())` |  
  
For Network Interceptor, change `addInterceptor` to `addNetworkInterceptor`  
  
To pass `Moesif Application Id` directly in lieu of setting as environment variable:  
Change `MoesifOkHttp3Interceptor()` to `MoesifOkHttp3Interceptor("Your Moesif Application Id here")`   
The [Official OkHttp3 recipe for Synchronous Get](https://square.github.io/okhttp/recipes/#synchronous-get-kt-java)  
has been modified below.  
  
### Example (Kotlin)  
  
```bash  
$ export MOESIF_APPLICATION_ID = "Your Moesif App Id Here"  
```  
  
```kotlin  
import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor

val client = OkHttpClient.Builder()
 .addInterceptor(MoesifOkHttp3Interceptor()) // The only modification to official sample
 .build()

val request = Request.Builder()  
 .url("https://publicobject.com/helloworld.txt")
 .build()  

client.newCall(request).execute().use { response ->  
  if (!response.isSuccessful) throw IOException("Unexpected code $response")  
  for ((name, value) in response.headers) {
    println("$name: $value")
  }
println(response.body!!.string())}
```  
  
### Example (Java)  
  
```bash  
$ export MOESIF_APPLICATION_ID = "Your Moesif App Id Here"  
```  
```java  
import com.moesif.sdk.okhttp3client.MoesifOkHttp3Interceptor;  
  
private final OkHttpClient client = new OkHttpClient.Builder()  
 .addInterceptor(new MoesifOkHttp3Interceptor())  // The only modification to official sample
 .build();  

public void run() throws Exception {  
 Request request = new Request.Builder()
   .url("https://publicobject.com/helloworld.txt")
   .build();  
 try (Response response = client.newCall(request).execute()) {
   if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
   Headers responseHeaders = response.headers();
   for (int i = 0; i < responseHeaders.size(); i++) {
      System.out.println(responseHeaders.name(i) + ": " +  responseHeaders.value(i));
   }
   System.out.println(response.body().string()); 
   }
}  
```  
  
## Obtaining your Moesif Application Id  
Your Moesif Application Id can be found in the [_Moesif Portal_](https://www.moesif.com/).  
After signing up for a Moesif account, your Moesif Application Id will be displayed during the onboarding steps.   
  
You can always find your Moesif Application Id at any time by logging   
into the [_Moesif Portal_](https://www.moesif.com/), click on the top right menu,  
and then clicking _Installation_.  
  
## Advanced Configuration options
By default, all events are submitted, and no content is masked.
This default behavior is captured in the default config file `DefaultEventFilterConfig.java`

This behavior can be overwritten by creating a custom config class that implements `IInterceptEventFilter`.

Here is a sample customized config file "MyCustomEventFilterConfig.java"
```java
public static class MyCustomEventFilterConfig implements IInterceptEventFilter{

    @Override
    public EventModel maskContent(EventModel eventModel) {
        if (eventModel.getRequest().getIpAddress() == "127.0.0.1")
            eventModel.getRequest().setIpAddress("127.0.0.2");
        return eventModel;
    }

    @Override
    public boolean skip(Request request, Response response) {
        return request.method() == "DELETE";
    }

    @Override
    public Optional<String> identifyUser(Request request, Response response) {
        return Optional.of("customUser");
    }

    @Override
    public Optional<String> identifyCompany(Request request, Response response) {
        return Optional.of("customCompany");
    }

    @Override
    public Optional<String> sessionToken(Request request, Response response) {
        return Optional.of("customSessionToken");
    }

    @Override
    public @Nullable Map<String, Object> getMetadata(Request request, Response response) {
        Map<String, Object> customMetadata = new HashMap<String, Object>();
        Map<String, Object> subObject = new HashMap<String, Object>();
        subObject.put("destructive_method", request.method() == "DELETE");
        customMetadata.put("cost_center", "a554411");
        customMetadata.put("retention_months", 12);
        customMetadata.put("method_detais", subObject);
        return customMetadata;
    }

    @Override
    public Optional<String> getApiVersion(Request request, Response response) {
        return Optional.of("v-3.1415");
    }
}
```
To use this custom config, update it prior to constructing the interceptor
```java
MoesifApiConnConfig cfg = new MoesifApiConnConfig();
cfg.setEventFilterConfig(new MyCustomEventFilterConfig());
MoesifOkHttp3Interceptor interceptor = new MoesifOkHttp3Interceptor(cfg);

```

### Credits:  
`com.moesif.external.facebook.stetho.inspector.network` contains code borrowed (and modified for Moesif) from Facebook/Stetho [Official site](https://facebook.github.io/stetho/) | [Code on Github](https://github.com/facebook/stetho)  
*Thank you Facebook/Stetho!*  
  
### Built for OkHttp 
[Official Readme](https://square.github.io/okhttp/) | [Code on Github](https://github.com/square/okhttp)  
*Thank you Square/OkHttp!*