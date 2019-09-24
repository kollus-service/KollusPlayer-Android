### Kollus Player SDK Android Sample

1. SDK Setting
- SDK 인증정보 입력
```java

//위치 /app/src/main/java/kollus/test/media/constant/KollusConstant.java
// 인증키
    public static final String KOLLUS_SDK_KEY = "99c8d382df6ec620746a086db0b09d58f6eaa019";
// 만료 일자
    public static final String KOLLUS_SDK_EXPIRE_DATE = "2019/12/31";
    // 앱 패키지명
    public static final String KOLLUS_SDK_PACKAGE_NAME = "kollus.test.media";
```

- 콜러스 API 및 플레이 URL 생성 관련키

```java

//JWT 토큰 생성을 위한 비밀키
    public static final String KOLLUS_SECRET_KEY = "";
//URL 사용을 위한 사용자키
    public static final String KOLLUS_CUSTOM_KEY = "";
    //API 사용을 위한 API 토큰
    public static final String KOLLUS_API_KEY = "";

```

2. 주요 페캐지

- player
* 플레이어 UI 및 기능구현되어 있는 패키지
- kollusapi
* 컨텐츠 목록 조회 및 재생 URL 생성 클래스가 있는 패키지