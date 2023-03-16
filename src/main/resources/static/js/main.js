//지도 전역변수 초기화
var map;
$(document).ready(function () {
    // 시작 날짜를 datepicker로 초기화
    $('#start-date').datepicker({
        format: 'yyyy-mm-dd',
        autoclose: true,
        startDate: new Date()
    });

    // 종료 날짜를 datepicker로 초기화
    $('#exit-date').datepicker({
        format: 'yyyy-mm-dd',
        autoclose: true,
        startDate: new Date()
    });

    // 검색어 input에서 Enter 키 입력 시 이벤트
    $('#keyword').keypress(function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            searchData();
        }
    });

    //찾기 버튼을 누를경우 이벤트
    $('#SearchButton').click(function () {
        searchData();
    });

    //현위치를 누르면 검색창 비활성화
    $("#mylocation").change(async function () {
        const keyword = $('#keyword');
        if ($("#mylocation").is(":checked")) {
            try {
                const position = await getCurrentPosition();
                keyword.val("");
                keyword.attr("readonly", true);
                var moveLatLon = new kakao.maps.LatLng(position.coords.latitude, position.coords.longitude);
                map.panTo(moveLatLon);
            } catch (error) {
                alert("현재 위치 기반 검색을 하시려면 위치정보를 켜주세요!");
                $("#mylocation").prop("checked", false);
                return;
            }
        } else {
            keyword.attr("readonly", false);
        }
    });

    //예약버튼을 누를경우 이벤트
    $('#parking_reservation_check').click(function () {
        const id = $('#parking-lot-id').val();
        const startDate = $('#start-date').val();
        const startTime = $('#start-time').val();
        const endDate = $('#exit-date').val();
        const endTime = $('#exit-time').val();
        if (id == "" ){
            alert("주차장을 선택해 주세요.")
            return false;
        }
        if (startDate == "" || startTime == "" || endDate == "" || endTime == "") {
            alert("예약 시간을 확인해주세요.")
            return false;
        }

        //받은 시간값을 DateTime형식으로 변환
        const startDateTime = startDate + "T" + startTime;
        const endDateTime = endDate + "T" + endTime;
        if (startDateTime >= endDateTime) {
            alert("종료 시간이 시작 시간보다 같거나 빠릅니다.");
            return false;
        }
        const body = {
            startDate: startDateTime,
            endDate: endDateTime,
        };
        const params = new URLSearchParams(body).toString();
        // 주차예약 버튼 클릭 시 모달창 띄우기
        $('#reservation-modal').modal('show');
        axios.get(`/api/booking/${id}?${params}`)
            .then(response => {
                const data = response.data;
                $("#parking-lot-available-modal").attr("value", data.data.available);
                $("#parking-lot-booking-modal").attr("value", data.data.booking);
                $("#parking-lot-price-modal").attr("value", data.data.charge+"원");

            })
            .catch(error => {
                alert("조회 에러입니다")
                return false;
            });


    });

    //처음 접속 시 지도 기본값 출력
    var mapContainer = document.getElementById('map'),
        mapOption = {
            // 지도의 중심 좌표
            center: new kakao.maps.LatLng("37.5546788388674", "126.970606917394"),
            // 지도의 확대 레벨
            level: 3
        };
    // 지도 생성 및 객체 리턴
    map = new kakao.maps.Map(mapContainer, mapOption);
});

//navigator.geolocation은 비동기 방식이라 동기방식으로 변경하기위해 promise 사용
function getCurrentPosition() {
    return new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(
            position => {
                resolve(position);
            },
            error => {
                reject(error);
            }
        );
    });
}

async function searchData() {
    const parktime = $('#parktime').val();
    const charge = $('#charge').val();
    const type = $('#type').val();
    const available = $('#available').prop('checked');
    const mylocation = $('#mylocation').prop('checked');
    const keyword = $('#keyword').val();
    let url = "";
    if (!mylocation && keyword == "") {
        alert("검색어를 입력해주세요");
        return;
    }
    if (mylocation) {
        //크롬 위치 정보 동의 하지 않을경우 catch로 나오게 됨
        try {
            const position = await getCurrentPosition();
            var la = position.coords.latitude;
            var lo = position.coords.longitude;
        } catch (error) {
            alert("현재 위치 기반 검색을 하시려면 위치정보를 켜주세요!");
            return;
        }
        url = `/api/parks?parktime=${parktime}&charge=${charge}&type=${type}&available=${available}&la=${la}&lo=${lo}`;
    } else {
        url = `/api/parks?keyword=${keyword}&parktime=${parktime}&charge=${charge}&type=${type}&available=${available}`;
    }
    axiosMapRenderFromKakao(url);
}

function axiosMapRenderFromKakao(url) {
    //axios로 URL을 호출
    axios.get(url)
        .then(function (response) {
            const data = response.data.data;
            console.log(data);
            //받은 데이터가 0개 일때 초기위치를 data배열에 넣어준다
            if (data.length == 0) {
                data.push({la: "37.5546788388674", lo: "126.970606917394"});
                alert("결과가 없습니다");
            }

            // 지도를 표시할 div
            var mapContainer = document.getElementById('map'),
                mapOption = {
                    // 지도의 중심 좌표
                    center: new kakao.maps.LatLng(data[0].la, data[0].lo),
                    // 지도의 확대 레벨
                    level: 3
                };

            // 지도 생성 및 객체 리턴
            map = new kakao.maps.Map(mapContainer, mapOption);

            // 마커 이미지 생성
            var markerImage = new kakao.maps.MarkerImage('/img/location.png', new kakao.maps.Size(50, 50), {
                offset: new kakao.maps.Point(25, 26)
            });
            // 결과값이 한 화면에 보이게 LatLngBounds 객체 생성
            var bounds = new kakao.maps.LatLngBounds();
            // 받아온 데이터를 이용하여 마커 생성
            data.forEach(function (item) {
                //bound에 좌표값 추가
                bounds.extend(new kakao.maps.LatLng(item.la, item.lo));
                var marker = new kakao.maps.Marker({
                    // 마커를 표시할 지도 객체
                    map: map,
                    // 마커의 위치 좌표
                    position: new kakao.maps.LatLng(item.la, item.lo),
                    // 마커에 마우스를 올렸을 때 나타날 툴팁 메시지
                    title: item.name,
                    // 마커 이미지 설정
                    image: markerImage
                });

                //클릭 이벤트 리스너가 작동되면 해당 주차장의 이름을 input 요소에 표시
                var handleClickMarker = function () {
                    $("#parking-lot-name").attr("value", item.name);
                    //도로명주소가 없을경우 지번주소로 입력
                    let address = item.address1;
                    if (address == "") {
                        address = item.address2;
                    }
                    $("#parking-lot-id").attr("value", item.id);
                    $("#parking-lot-address").attr("value", address);
                    $("#parking-lot-price").attr("value", item.totCharge+"원");
                    $("#parking-lot-total-spots").attr("value",item.cmprtCo+"대");
                    $("#parking-lot-operation-hours").attr("value",item.weekdayOpen+" ~ "+item.weekdayClose);
                    $("#parking-lot-basic-price").attr("value",item.chargeBsTime+"분 "+item.chargeBsChrg+"원");
                    $("#parking-lot-additional-price").attr("value",item.chargeAditUnitTime+"분당 "+item.chargeAditUnitChrg+"원");
                };

                // 마커에 클릭 이벤트 리스너 추가
                kakao.maps.event.addListener(marker, 'click', handleClickMarker);
            });
            //bound를 셋팅하여 결과가 한 화면에 보이게 설정
            map.setBounds(bounds);
        });

}
