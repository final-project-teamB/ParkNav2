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
            window.location.href="/main";
        }
        return Promise.reject(error);
    });

    $("#parking-list").empty();
    if (token && token !== '') {
        axios.defaults.headers.common['Authorization'] = token;
        axios.get("/api/booking/mypage")
            .then(response => {
                const data = response.data.data;
                console.log(data);
                data.map((item)=>{
                    const button ="<button type=\"button\" class=\"btn btn-outline-secondary btn-sm mx-1\" onclick=\"viewReservation()\">예약 확인</button>" +
                        "<button type=\"button\" class=\"btn btn-outline-secondary btn-sm mx-1\" onclick=\"cancelReservation()\">예약 취소</button>";
                    $("#parking-list").append(`
                    <tr>
                        <td>${item.parkName}</td>
                        <td>${item.carNum}</td>
                        <td>${item.startDate} ~<br> ${item.endDate}</td>
                        <td>${item.charge+"원"}</td>
                        <td>
                            ${button}
                        </td>
                    </tr>`
                    );
                })
                if(data.length==0){
                    $("#parking-list").append(`<tr><td colspan="5">데이터가 없습니다</td></tr>`)
                }
            })
            .catch(error => {
                alert(error.response.data.error.msg)
                return false;
            });

    }else{
        alert("로그인이 필요합니다");
        window.location.href = "/main";
        $("#parking-list").append(`<tr><td colspan="6">데이터가 없습니다</td></tr>`)
    }

    //마이페이지 -> 메인페이지 이동
    $('#main-button').click(function () {
        window.location.href="/main";
    });

    //차량등록 버튼 클릭 이벤트
    $('#register-button').click(function () {
        myCarList();
    });

    //라디오버튼 이벤트
    $(document).on('click', 'input[type="radio"]', function() {
        $('input[type="radio"]').not(this).prop('checked', false);
        const carNum = $(this).closest('tr').find('td:nth-child(2)').text();
        axios.put("/api/car/rep",{carNum : carNum})
            .then(response => {
                alert(response.data.msg);
            })
            .catch(error => {
                console.log(error.response.data.error.msg)
                return false;
            });
    });

    //삭제버튼 클릭 이벤트
    $('#existing-car-numbers').on('click', '[name=car-delete-button]', function() {
        const carNum = $(this).closest('tr').find('td:eq(1)').text();
        if (confirm(carNum+" 차량을 삭제하시겠습니까?")) {
            axios.delete("/api/car/reg",{data: {carNum : carNum}})
                .then(response => {
                    alert(response.data.msg);
                    myCarList();
                })
                .catch(error => {
                    console.log(error.response.data.error.msg)
                    return false;
                });
        }
    });

    //모달 차량번호 등록
    $('#car-number-confirm').click(function () {
        const carnum = $("#car-number-input").val();
        const pattern1 = /^[0-9]{2,3}[가-힣]{1}[0-9]{4}$/;
        const pattern2 = /^[가-힣]{2}[0-9]{2}[가-힣]{1}[0-9]{4}$/;
        if (pattern1.test(carnum)||pattern2.test(carnum)) {
            axios.post(`/api/car/reg`, {carNum:carnum})
                .then(response => {
                    console.log(response)
                    if(response.data.msg === "차량 등록 성공") {
                        alert(response.data.msg);
                        myCarList();
                    } else {
                        alert("등록에 실패했습니다.")
                        return false;
                    }
                })
                .catch(error => {
                    alert(error.response.data.error.msg)
                    return false;
                });
        } else {
            alert("올바른 차량번호 형식이 아닙니다.");
        }
    });

    $('button.btn-danger').click(function() {
        const carnum = $(this).parent().prev().prev().text();
        console.log(carnum); // 다라마1234
    });

});

function myCarList(){
    $("#existing-car-numbers").empty();
    axios.get("/api/car/check")
        .then(response => {
            const data = response.data.data;
            console.log(data);
            let num = 1;
            data.map((item)=>{
                $("#existing-car-numbers").append(`
                    <tr>
                        <td>${num++}</td>
                        <td>${item.carNum}</td>
                        <td>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" ${num===2?"checked":null}>
                        </div>
                        </td>
                        <td><button class="btn btn-outline-danger mx-2" name="car-delete-button">삭제</button></td>
                    </tr>`
                );
            })
            if(data.length==0){
                $("#existing-car-numbers").append(`<tr><td colspan="4">등록 된 차량이 없습니다</td></tr>`)
            }
        })
        .catch(error => {
            console.log(error.response.data.error.msg)
            return false;
        });
}