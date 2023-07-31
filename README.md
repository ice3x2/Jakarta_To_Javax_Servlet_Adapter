# Jakarta_To_Javax_Servlet_Adapter
Using this adapter module, you can use javax servlet web app in Jakarta servlet WAS.
This module provides support for using legacy web applications on web application servers (WAS) that implement the jakarta.servlet package. As a result, HttpServletRequest and HttpServletResponse from the javax.servlet package can be used on WAS that supports Servlet 5 or above.

# How to use
Add servlet 4.0 to dependencies in build.gradle.
```groovy
dependencies {
    implementation 'javax.servlet:javax.servlet-api:4.0.1'
}
```

javax.servlet -> jakarta.servlet
```java
public class ExampleServlet extends jakarta.servlet.http.HttpServlet {
     @Override
     protected void doGet(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response) throws ServletException, IOException {
            // You can now work with javax.servlet classes
            javax.servlet.http.HttpServletResponse javaxResponse = ServletAdapter.adaptToJavax(response);
            javax.servlet.http.HttpServletRequest javaxRequest = ServletAdapter.adaptToJavax(request);
            javax.servlet.ServletContext javaxContext = ServletAdapter.adaptToJavax(getServletContext());
      }
}
```


It is possible to convert the jakarta.servlet classes to javax.servlet. However, this behavior is not guaranteed to be reliable, as your servlet version may exceed 5.0. This module is written based on Servlet 5.0. 
jakarta.servlet -> javax.servlet
```
public class ExampleServlet extends javax.servlet.http.HttpServlet {
    @Override
    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws ServletException, IOException {
          jakarta.servlet.http.HttpServletResponse jakartaResponse = ServletAdapter.adaptToJakarta(response);
          jakarta.servlet.http.HttpServletRequest jakartaRequest = ServletAdapter.adaptToJakarta(request);
          jakarta.servlet.ServletContext jakartaContext = ServletAdapter.adaptToJakarta(getServletContext())
   }
}
```



