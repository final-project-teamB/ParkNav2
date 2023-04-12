//지도 전역변수 초기화
var map;
var searchParktime;
var searchCharge;
const token = localStorage.getItem('Authorization');
$(document).ready(function () {
    axios.interceptors.response.use(function (response) {
        // 응답 성공 직전 호출되는 콜백
        return response;
    }, function (error) {
        // 응답 에러 직전 호출되는 콜백
        const errorMsg = error.response.data?.error?.msg;
        if (errorMsg === "토큰이 유효하지 않습니다." || errorMsg === "토큰이 없습니다." || errorMsg === undefined) {
            alert("로그인이 만료 되었습니다 재 로그인 해주세요")
            localStorage.removeItem("Authorization");
            window.location.href = "/main";
            return false;
        }
        return Promise.reject(error);
    });

    // 현재 날짜를 가져오기
    var now = new Date();
    var dd = String(now.getDate()).padStart(2, '0');
    var mm = String(now.getMonth() + 1).padStart(2, '0');
    var yyyy = now.getFullYear();
    // 날짜를 yyyy-mm-dd 형식으로 조합
    var todayStr = yyyy + '-' + mm + '-' + dd;
    // 시작 날짜 input의 기본값을 오늘 날짜로 설정
    $('#start-date').val(todayStr);
    // 종료 날짜 input의 기본값을 오늘 날짜로 설정
    $('#exit-date').val(todayStr);
    // 시작 시간과 종료 시간을 설정합니다.
    var startTime = now.getHours() < 10 ? '0' + now.getHours() + ':00:00' : now.getHours() + ':00:00';
    // 시작 시간과 종료 시간의 옵션 중 선택된 것을 해제합니다.
    $('#start-time, #exit-time').find('option:selected').prop('selected', false);
    // 현재 시간에 해당하는 시작 시간의 옵션을 선택합니다.
    $('#start-time').find('option[value="' + startTime + '"]').prop('selected', true);
    // 현재 시간 + 1시간에 해당하는 종료 시간의 옵션을 선택합니다.
    $('#exit-time').find('option[value="' + startTime + '"]').prop('selected', true);

    if (token && token !== '') {
        axios.defaults.headers.common['Authorization'] = token;
        $("#login-button").hide();
        $("#signup-button").hide();
        $("#logout-button").show();
        $("#mypage-button").show();
    } else {
        $("#loginModal").modal('show');
        $("#login-button").show();
        $("#signup-button").show();
        $("#logout-button").hide();
        $("#mypage-button").hide();
    }
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
        if (id == "") {
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
        const now = new Date();
        const startDateTimetoDate = new Date(startDateTime);
        // 년월일시까지만 비교할 현재 시간
        const nowTimeHours = new Date(now.getFullYear(), now.getMonth(), now.getDate(), now.getHours());
        // 년월일시까지만 비교할 사용자 입력 시간
        const userStartDateTimeHours = new Date(startDateTimetoDate.getFullYear(), startDateTimetoDate.getMonth(), startDateTimetoDate.getDate(), startDateTimetoDate.getHours());

        if (startDateTime >= endDateTime) {
            alert("종료 시간이 시작 시간보다 같거나 빠릅니다.");
            return false;
        }
        if (startDateTimetoDate.getTime() < nowTimeHours.getTime()) {
            alert("현재 시간 이후로만 선택 가능합니다")
            return false;
        }
        const body = {
            startDate: startDateTime,
            endDate: endDateTime,
        };
        const params = new URLSearchParams(body).toString();
        // 주차예약 버튼 클릭 시 모달창 띄우기
        $('#reservation-modal').modal('show');
        $("#parking-lot-booking-modal").attr("class","alert alert-warning")
        $('#parking-lot-booking-modal').text("운영시간이 아닙니다");
        $("#parking-lot-not-allowed-time-div").hide();
        $("#parking_reservation").hide();
        axios.get(`/api/booking/${id}?${params}`)
            .then(response => {
                console.log(response)
                const data = response.data.data;
                $("#parking-lot-not-allowed-time").empty();
                if (data.isOperation === false) {
                    $("#parking-lot-booking-modal").attr("class","alert alert-warning").text("운영시간이 아닙니다");
                    $("#parking-lot-not-allowed-time-div").hide();
                    $("#parking-lot-price-modal-div").hide();
                    $("#parking_reservation").hide();
                } else if (data.notAllowedTimeList.length > 0) {
                    $("#parking-lot-booking-modal").attr("class","alert alert-danger").text("예약불가 시간이 있습니다");
                    $("#parking-lot-not-allowed-time-div").show();
                    $("#parking-lot-price-modal-div").hide();
                    data.notAllowedTimeList.map(s => {
                        $("#parking-lot-not-allowed-time").append("<option>" + s.replace("T", " ") + "</option>")
                    })
                    $("#parking_reservation").hide();
                } else {
                    $("#parking-lot-booking-modal").attr("class","alert alert-success").text("예약이 가능합니다");
                    $("#parking_reservation").show();
                    $("#parking-lot-not-allowed-time-div").hide();
                    $("#parking-lot-price-modal-div").show();
                    $("#parking-lot-price-modal").attr("value", data.charge);
                }
            })
            .catch(error => {
                alert("조회 에러입니다")
                return false;
            });
    });

    $('#parking_reservation').click(function () {
        const id = $("#parking-lot-id").val();
        const startDate = $('#start-date').val() + "T" + $('#start-time').val();
        const endDate = $('#exit-date').val() + "T" + $('#exit-time').val();
        const body = {
            startDate: startDate,
            endDate: endDate
        }

        axios.post(`/api/booking/${id}`, body)
            .then(response => {
                if (response.data.msg === "예약이 완료되었습니다.") {
                    alert(response.data.msg);
                    $('#reservation-modal').modal('hide');
                } else {
                    alert("예약에 실패했습니다.")
                    return false;
                }
            })
            .catch(error => {
                alert(error.response.data.error.msg);
                return false;
            });
    });

    //모달 로그인 버튼 클릭시
    $('#login-btn').click(function () {
        userLogin();
    });

    //모달 비밀번호에서 Enter 키 입력 시 이벤트
    $('#password').keypress(function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            userLogin();
        }
    });

    //모달 아이디에서 Enter 키 입력 시 이벤트
    $('#username').keypress(function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            $('#password').focus();
        }
    });

    //로그아웃 버튼 클릭 이벤트
    $('#logout-button').click(function () {
        if (confirm("로그아웃 하시겠습니까?")) {
            localStorage.setItem('Authorization', '');
            window.location.reload();
        } else {
            return false;
        }
    });

    //마이페이지 버튼 클릭 이벤트
    $('#mypage-button').click(function () {
        window.location.href = "/mypage";
    });

    //모달 회원가입 버튼 클릭 이벤트
    $('#signup-btn').click(function () {
        const id = $('#signup_username');
        const password = $('#signup_password');
        const passwordCheck = $('#signup_password_check');

        if (id.val() === "") {
            alert("아이디를 입력해 주세요.");
            id.focus();
            return false;
        }
        if (password.val() === "") {
            alert("비밀번호를 입력해 주세요.");
            password.focus();
            return false;
        }
        if (passwordCheck.val() === "") {
            alert("비밀번호 확인을 입력해 주세요.");
            passwordCheck.focus();
            return false;
        }
        if (password.val() !== passwordCheck.val()) {
            alert("비밀번호 확인이 다릅니다.");
            passwordCheck.focus();
            return false;
        }

        const body = {
            userId: id.val(),
            password: password.val()
        };
        axios.post(`/api/users/signup`, body)
            .then(response => {
                if (response.data.msg === "회원가입이 완료되었습니다.") {
                    alert(response.data.msg);
                    $('#signupModal').modal('hide');
                    id.val("");
                    password.val("");
                    passwordCheck.val("");
                } else {
                    alert("회원가입에 실패했습니다.")
                    return false;
                }
            })
            .catch(error => {
                alert(error.response.data.error.msg);
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
        url = `/api/parks?parktime=${parktime}&charge=${charge}&type=${type}&la=${la}&lo=${lo}`;
    } else {
        url = `/api/parks?keyword=${keyword}&parktime=${parktime}&charge=${charge}&type=${type}`;
    }
    axiosMapRenderFromKakao(url);
    searchParktime = parktime;
    searchCharge = charge;
}

function userLogin() {
    const userId = $("#username").val();
    const password = $("#password").val();
    if (userId === "") {
        alert("아이디를 입력해주세요");
        $("#username").focus();
        return false;
    }
    if (password === "") {
        alert("패스워드를 입력해주세요");
        $("#password").focus();
        return false;
    }
    const body = {
        userId: userId,
        password: password,
    };

    axios.post("/api/users/login", body)
        .then(response => {
            if (response.data.msg === '로그인이 완료되었습니다.') {
                const token = response.headers.authorization;
                axios.defaults.headers.common['Authorization'] = response.headers.authorization;
                localStorage.setItem('Authorization', token);
                window.location.reload();
            } else {
                alert('로그인에 실패하셨습니다. 다시 로그인해 주세요.')
                return false;
            }
        })
        .catch(error => {
            alert(error.response.data.error.msg);
            return false;
        });
}

//카카오 맵 지도 호출
function axiosMapRenderFromKakao(url) {
    //axios로 URL을 호출
    axios.get(url)
        .then(function (response) {
            let data = [];
            let centerLa, centerLo, placeName;
            let result = false;
            if (response.data && response.data.data && response.data.data.parkOperInfoDtos) {
                data = response.data.data.parkOperInfoDtos;
                centerLa = response.data.data.la;
                centerLo = response.data.data.lo;
                placeName = response.data.data.placeName;
                result = true;
                //받은 데이터가 0개 일때 초기위치를 data배열에 넣어준다
                if (data.length == 0) {
                    data.push({la: centerLa, lo: centerLo});
                    alert("결과가 없습니다");
                    result = false;
                }
            } else {
                centerLa = "37.5546788388674";
                centerLo = "126.970606917394";
                placeName = "서울역";
                data.push({la: centerLa, lo: centerLo});
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
            // 지도 확대 축소를 제어할 수 있는  줌 컨트롤을 생성합니다
            var zoomControl = new kakao.maps.ZoomControl();
            map.addControl(zoomControl, kakao.maps.ControlPosition.RIGHT);

            // 마커 이미지 생성
            var markerImage = new kakao.maps.MarkerImage('/img/location.png', new kakao.maps.Size(25, 25), {
                offset: new kakao.maps.Point(10, 10)
            });
            //중심 좌표 마커이미지 생성
            var centerMarkerImage = new kakao.maps.MarkerImage('/img/clocation.png', new kakao.maps.Size(45, 45), {
                offset: new kakao.maps.Point(20, 21)
            });
            // 받아온 데이터를 이용하여 마커 생성
            var marker = new kakao.maps.Marker({
                // 마커를 표시할 지도 객체
                map: map,
                // 마커의 위치 좌표
                position: new kakao.maps.LatLng(centerLa, centerLo),
                // 마커에 마우스를 올렸을 때 나타날 툴팁 메시지
                title: placeName,
                // 마커 이미지 설정
                image: centerMarkerImage
            });
            var markers = []
            var i = 0;
            if (result) {
                data.forEach(function (item) {
                    markers.push(new kakao.maps.Marker({
                        // 마커를 표시할 지도 객체
                        map: map,
                        // 마커의 위치 좌표
                        position: new kakao.maps.LatLng(item.la, item.lo),
                        // 마커에 마우스를 올렸을 때 나타날 툴팁 메시지
                        title: item.name,
                        // 마커 이미지 설정
                        image: markerImage
                    }));
                    //클릭 이벤트 리스너가 작동되면 해당 주차장의 정보을 input 요소에 표시
                    var handleClickMarker = function () {
                        $("#parking-lot-id").attr("value", item.parkInfoId);
                        $("#parking-lot-name").attr("value", item.name);
                        const body = {
                            parkInfoId: item.parkInfoId,
                            parktime: searchParktime,
                            charge: searchCharge
                        };
                        const params = new URLSearchParams(body).toString();
                        axios.get(`/api/parks/oper-info?${params}`)
                            .then(response => {
                                operData = response.data.data;
                                //도로명주소가 없을경우 지번주소로 입력
                                let address = operData.address1;
                                if (address == "") {
                                    address = operData.address2;
                                }
                                $("#parking-lot-address").attr("value", address);
                                $("#parking-lot-price").attr("value", operData.totCharge + "원");
                                $("#parking-lot-total-spots").attr("value", operData.cmprtCo + "대");
                                $("#parking-lot-operation-hours").attr("value", operData.weekdayOpen + " ~ " + operData.weekdayClose);
                                $("#parking-lot-basic-price").attr("value", operData.chargeBsTime + "분 " + operData.chargeBsChrg + "원");
                                $("#parking-lot-additional-price").attr("value", operData.chargeAditUnitTime + "분당 " + operData.chargeAditUnitChrg + "원");
                                $("#parking-lot-available").attr("value", operData.available);
                                $("#weekOpen").text("평일: " + operData.weekdayOpen + " ~ " + operData.weekdayClose);
                                $("#satOpen").text("토요일: " + operData.satOpen + " ~ " + operData.satClose);
                                $("#sunOpen").text("휴일: " + operData.sunOpen + " ~ " + operData.sunClose);
                            })
                            .catch(error => {
                                alert("조회 에러입니다")
                                return false;
                            });
                    };
                    // 마커에 클릭 이벤트 리스너 추가
                    kakao.maps.event.addListener(markers[i++], 'click', handleClickMarker);
                });
            }
            // 클러스터러를 생성합니다.
            var clusterer = new kakao.maps.MarkerClusterer({
                map: map,
                markers: markers,
                gridSize: 50,
                averageCenter: true,
                minLevel: 4
            });
            // 클러스터 클릭 이벤트를 추가합니다.
            kakao.maps.event.addListener(clusterer, 'clusterclick', function (cluster) {
                var level = map.getLevel() - 1;
                map.setLevel(level, {anchor: cluster.getCenter()});

            });

        });
}
