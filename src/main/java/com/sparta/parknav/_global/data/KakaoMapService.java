package com.sparta.parknav._global.data;

import com.sparta.parknav.parking.dto.KakaoSearchDto;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/* KAKAO MAP API를 이용하여 주소를 입력하면 위도, 경도를 리턴하는 getCoordinates 메서드 구현 */
@Service
public class KakaoMapService {

    @Value("${kakao.api.key}")
    private String API_KEY;

    // address 를 파라미터로 보내면 {"latitude" : 위도값}을 담는 Map 을 리턴한다.
    public Map<String, Double> getCoordinates(String address) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + API_KEY);

        // 검색어를 인코딩하여 URL 에 붙인다.
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + URLEncoder.encode(address, StandardCharsets.UTF_8);

        // url 에 GET 요청을 보낸 응답으로 Map 객체를 받아오기
        RequestEntity<?> request = RequestEntity.get(URI.create(url)).headers(headers).build();
        ResponseEntity<Map> response = restTemplate.exchange(request, Map.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            JSONArray jsonArray = (JSONArray) jsonObject.get("documents");

            if (jsonArray != null) {
                try {
                    // 위도, 경도 정보가 있는 Object 선언
                    JSONObject tempObj = (JSONObject) jsonArray.get(0);

                    double latitude = tempObj.getDouble("y");
                    double longitude = tempObj.getDouble("x");

                    Map<String, Double> coordinates = new HashMap<>();
                    coordinates.put("latitude", latitude);
                    coordinates.put("longitude", longitude);

                    return coordinates;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public KakaoSearchDto getKakaoSearch(String searchKeyword) {
        //카카오 API키
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "KakaoAK " + API_KEY);

        //요청 URL과 검색어를 담음
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=" + searchKeyword;
        //RestTemplate를 이용해
        RestTemplate restTemplate = new RestTemplate();
        //HTTPHeader를 설정해줘야 하기때문에 생성함
        HttpEntity<?> entity = new HttpEntity<>(headers);

        //ResTemplate를 이용해 요청을 보내고 KakaoSearchDto로 받아 response에 담음
        ResponseEntity<KakaoSearchDto> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoSearchDto.class
        );

        return response.getBody();
    }
}
