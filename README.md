# jsug-2024-restclient 
> JSUG勉強会2024その2 ～ Spring Frameworkの新標準!? RestClientとHTTPインターフェース入門の登壇スライドで使っているサンプルアプリ一式

- RestClientやHTTPInterfaceを使ったRESTクライアントコードは`/src/main`側にあります
- RESTクライアントから呼び出される[サーバー側の実装](src/test/java/sample/spring/book/stub/)やRESTクライアントに対するテストコードは`/src/test`側にあります
- `/src/main`側に置いているのはクライアントコードのみで`@SpringBootApplication`クラスはありません。したがって、アプリとしては起動せず行えるのは`mvn test`のみとなっています