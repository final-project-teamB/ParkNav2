
![image](https://user-images.githubusercontent.com/96133075/231780028-57618253-9f6e-4657-9144-bf7bfbea650b.png)

# ParkNav 🚌
### 주차장을 찾아가는 네비게이션과 같이 쉽게 주차장을 조회하고 예약할 수 있는 플랫폼입니다.

- 빠르고 정확한 전국 주차장 조회 서비스
- 편리하고 안정적인 주차장 예약 서비스
- 효율적인 주차장 운영을 위한 주차장 입•출차 현황 관리 서비스

#

## ParkNav 브로슈어 & 팀노션

[📗 ParkNav 브로슈어](https://park-nav.notion.site/ParkNav-d2000f88d39e45d5bf736f3ff3ae8a4a)<br>
[📙 ParkNav Notion](https://park-nav.notion.site/park-nav/ParkNav-a926b8c75e9a4497a6876ff25af53f3d)

#
## 프로젝트 소개 
- 프로젝트 이름 : ParkNav
- 프로젝트 소개 : 예약과 주차의 효율성을 높이는 주차장 예약 플랫폼
- 프로젝트 목표
    - 데이터 수집 : 실제 주차장 데이터 수집으로 정확한 정보 제공 및 다양한 예약, 입차 출차 데이터 생성
    - 알고리즘 : 예약 안정성과 주차장 운영 효율성을 최대로 구현하는 알고리즘
    - 검색 성능 : 다양한 검색어에 대한 빠르고 정확한 검색 결과 제공
    - 동시성 제어 : 동시적으로 일어나는 다양한 입차, 출차 상황에 대하여 안전하고 정확하게 제어
- 구현 기능
    - 주차장 검색 및 조회
    - 주차장 예약 기능
    - 마이페이지 기능
    - 차량 관리 기능
    - 관리자 페이지 주차현황 관리
- 기간 :
    - 2023.03.10 ~ 2023.04.21
- 팀원 : <br>


|     | 이름   | GITHUB |
|-----|------|--------|
| BE  | 김도연  | https://github.com/dev-dykim |
| BE  | 오세영  | https://github.com/osy9536 |
| BE  | 이재훈  | https://github.com/Gem-o-b |



#

## 기술 스택 
<div align=center> 
  <img src="https://img.shields.io/badge/java-002266?style=for-the-badge&logo=java&logoColor=white"> 
  <img src="https://img.shields.io/badge/python-FFE400?style=for-the-badge&logo=python&logoColor=4374D9"> 
  <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
  <br>
  <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/springjpa-6DB33F?style=for-the-badge&logo=jpa&logoColor=white"> 
  <img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=jpa&logoColor=white"> 
  
 
  <br>

  <img src="https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white"> 
  <img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black"> 
  <img src="https://img.shields.io/badge/css-1572B6?style=for-the-badge&logo=css3&logoColor=white"> 
  <img src="https://img.shields.io/badge/axios-61DAFB?style=for-the-badge&logo=axios&logoColor=black">
  <img src="https://img.shields.io/badge/Thymeleaf-339933?style=for-the-badge&logo=Thymeleaf&logoColor=white">
  <br>

  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white">
  <img src="https://img.shields.io/badge/amazonaws-FF9436?style=for-the-badge&logo=amazonaws&logoColor=white">  
  <img src="https://img.shields.io/badge/amazon rds-61DAFB?style=for-the-badge&logo=amazonrds&logoColor=white">
  <img src="https://img.shields.io/badge/redis-DD0031?style=for-the-badge&logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"> 


  <br>
  <img src="https://img.shields.io/badge/github action-000000?style=for-the-badge&logo=githubaction&logoColor=white">
  <img src="https://img.shields.io/badge/junit5-F05032?style=for-the-badge&logo=junit5&logoColor=white">
  <img src="https://img.shields.io/badge/jmeter-000000?style=for-the-badge&logo=jmeter&logoColor=white">
</div>

#

## 주요 기능 소개 

| 주차장 조회  |                                                    주차장 예약                                                     |
|:-------:|:-------------------------------------------------------------------------------------------------------------:|
| ![조회](https://user-images.githubusercontent.com/111578825/231799138-15a1b191-accb-4a57-90af-0e4f8f25ce4e.gif) | ![예약](https://user-images.githubusercontent.com/111578825/231799385-7c07bfc9-12ac-4492-b98a-d1a64a236c0e.gif) |
| - 예약하고 싶은 위치 혹은 현재 위치 주변의 주차장을 검색 <br>- 장소 검색, 현위치 주변 검색 기능<br>- 필터 검색 기능(주차시간, 주차요금, 주차유형) |        - 선택한 주차장의 예약 기능<br>- 선택한 시간별 예상요금 기능<br>- 현재 예약 가능 차량 수 확인 기능<br>- 예약이 불가능한 경우 불가능한 시간대 확인 기능         |


|                                                      마이페이지                                                       |               차량관리               |
|:----------------------------------------------------------------------------------------------------------------:|:--------------------------------:|
| ![마이페이지](https://user-images.githubusercontent.com/111578825/231799364-2a9a8bc6-c7ed-4035-95e1-85761152da31.gif) | ![차량관리](https://user-images.githubusercontent.com/111578825/231799398-77078d2c-3560-467f-9acb-6b0d7c651eb4.gif)|
|                           - 예약, 사용내역 확인 기능(주차장 이름, 차량번호, 예약일시, 주차요금, 관리)<br>- 예약 취소 기능                           | - 내 차량 등록 기능<br>- 대표차량 등록 기능<br>- 차량 삭제 기능 |

|                                                      관리자페이지                                                       |
|:-----------------------------------------------------------------------------------------------------------------:|
| ![관리자페이지](https://user-images.githubusercontent.com/111578825/231799350-d1aa02d6-803a-4f39-8e67-c34dac3828a7.gif) |
|                                    - 입 출차 차량 목록 기능<br>- 입차 기능<br>- 출차 기능                                     |

#
## 시연영상 
[시연영상 바로가기]()

#
## 서비스 아키텍처 
![image](https://user-images.githubusercontent.com/96133075/231802682-82b6b5d7-b3ee-4171-a24c-fb817afa69d9.png)

<details>
<summary> 기술선택 이유 펼쳐보기 </summary>
<div markdown="1">

| 요구 사항    | 기술                                                                                                       | 기술 선택 이유                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|----------|----------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 동시성 제어   | 선택한 기술 :</br>- Lettuce 스핀락</br></br>선택지 :</br>- Pessimistic Locking</br>- Redisson 분산락</br>- Lettuce 스핀락 | Lettuce 스핀락</br>- Version2에서 시간대별 예약 현황 테이블을 만듦으로써 성능적인 면의 상승이 있었지만, 동시성 제어가 실패하게 되면 코어 로직에서 이전 Version1보다 치명적인 문제가 발생할 수 있음 </br>- Pessimistic Locking이 속도면에서 더 좋은 성능을 갖고 있지만, 다음과 같은 단점이 존재</br> • 동시에 많은 요청이오면 데드락(deadlock)이 발생할 가능성 </br>  • 특정 데이터에 락을거는 특성상 해당 로직의 추가적인 수정이 발생시 락의 위치를 바꾸거나 추가적인 락을 걸어야 함</br>  • 완벽한 동시성 제어가 되지 않을 수 있음</br>→ 동시성 제어의 속도면에서는 Pessimistic Locking이 더 좋은 성능일지라도, 전체적인 프로젝트의 안정성을 위해 Lettuce의 스핀락 사용 |
| CI/CD    | 선택한 기술 :</br>- GitHub Actions</br></br>선택지 :</br>- GitHub Actions</br>- Jenkins                          | GitHub Actions</br>- 무료이거나 비용이 저렴할 것</br>- 짧은 기간에 사용해야하기 때문에 러닝커브 및 예상 리소스가 낮을 것</br>- EC2에 배포가 가능해야하고 GIT과 연동이 될 것                                                                                                                                                                                                                                                                                                                       |
| Test     | 선택한 기술 :</br>- Jmeter, JUnit5                                                                            | Jmeter:</br>- 대용량 트래픽을 시뮬레이션할 수 있는 테스트 도구로써 Jmeter를 선정:</br>- 이를 통해 시스템이 정상적으로 대량의 요청을 처리할 수 있는지 확인, 병목 현상 발견:</br>:</br>JUnit5:</br>- 자바 언어를 기반으로 하는 유닛 테스트 도구:</br>- 단위 테스트 : 단위테스트를 통해 해당 메서드가 정상 작동하는지 확인:</br>- 통합 테스트 : 동시성 제어 기능을 테스트하고 다중 스레드 환경에서 안정성을 확인                                                                                                                                                                        |
| 검색 성능 개선 | 선택한 기술 : </br>- Fetch Join</br>- QueryDSL</br>- Fulltext index                                           | Fetch Join(N+1 문제)</br>- 부모 엔티티를 조회한 후 연관된 자식 엔티티를 조회하는 과정에서 부모 엔티티 수만큼 자식 엔티티를 조회하는 문제 해결</br></br>QueryDSL</br>- 자바 코드와 유사한 형태로 쿼리를 작성 가능 </br>    → 가독성 향상, 코드 재사용성 향상, 컴파일시 문법 오류 확인 가능</br>- 런타임에 쿼리를 조건에 따라 다르게 생성하고 실행해야 하는 동적쿼리 생성 가능</br></br>Fulltext index(ngram)</br>- ParkInfo의 name 컬럼에 index를 걺으로써 검색 속도 향상</br>- 특정 단어, 구 검색 정확성 향상                                                                                       |
| API      | 선택한 기술 :</br>- Kakao map API</br></br>선택지 :</br>- Kakao map API</br>- Naver map API                      | Kakao map API</br>- 선택기준 : 검색 시 다양한 조건에 의해 검색이 가능해야함 ( 건물명, 도로명, 주소 등)</br>- Naver map API : 주소에 대한 위도, 경도만 검색가능 ( 강남구, 서울시 등 )</br>- Kakao map API : 검색어에 대한 위도, 경도를 검색가능( 63빌딩, 강남구 등 )                                                                                                                                                                                                                                                 |

</div>
</details>

#
## ERD 👨🏻‍
<details>
<summary> 펼쳐보기 </summary>
<div markdown="1">  

![image](https://user-images.githubusercontent.com/96133075/231802720-bce7cf31-7553-4e7a-8981-6b1e873350f7.png)

</div>
</details>

#
## API 
<details>
<summary> 펼쳐보기 </summary>
<div markdown="1">  

![image](https://user-images.githubusercontent.com/96133075/231802763-02ec2b27-1ffa-4e21-862c-15dd3165ee37.png)

</div>
</details>

#
## 성능개선


- 주차장 검색 성능 개선  
  👉 <a href="https://park-nav.notion.site/fe13e60753af4749912bdb0128d6c7b8" target="_blank"> 주차장 검색 성능 개선 기록 보기 </a>
  <br>
  👉 <a href="https://park-nav.notion.site/93a55b37bc8441e294581fcea80c0a4a" target="_blank"> 주차장 조회 성능 테스트 보기 </a>

- 예약 알고리즘 성능 개선  
  👉 <a href="https://park-nav.notion.site/ed0c8d7d03f44b1b84df5864747719a5" target="_blank"> 예약 알고리즘 성능 개선 기록 보기 </a>

- 동시성 제어 성능 테스트  
  👉 <a href="https://park-nav.notion.site/8462a3eb82484bb7bb3a464e61f4ea31" target="_blank"> 동시성제어 성능 테스트 보기 </a>

#
## 트러블슈팅 

- 예약 알고리즘 version2에 이르기까지 과정  
  👉 <a href="https://www.notion.so/park-nav/152d353a96734bb09175621f933c551e?pvs=4" target="_blank"> version2 알고리즘 트러블슈팅 보기 </a>

- 테스트코드에 대한 고민  
  👉 <a href="https://park-nav.notion.site/Jacoco-0c8a8ee50fb54c17b77e6c45950fab79" target="_blank"> 코드 커버리지 적용 보기 </a>
  <br>
  👉 <a href="https://park-nav.notion.site/7011b2513bdd4589b295f69a0b671db0" target="_blank"> 테스트코드 작성 트러블슈팅 보기 </a>

- Redis SpinLock 적용  
  👉 <a href="https://park-nav.notion.site/7ec0d3c0bef6487baacd605179d75110" target="_blank"> Redis SpinLock 적용 보기 </a>


