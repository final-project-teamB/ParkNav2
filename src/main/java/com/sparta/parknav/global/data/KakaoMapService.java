package com.sparta.parknav.global.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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
}
