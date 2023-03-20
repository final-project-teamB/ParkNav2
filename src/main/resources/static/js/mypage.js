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
            window.location.reload();
        }
        return Promise.reject(error);
    });

    //라디오버튼 이벤트
    $('input[type="radio"]').click(function() {
        $('input[type="radio"]').not(this).prop('checked', false);
    });

    $("#parking-list").empty();
    if (token && token !== '') {
        axios.defaults.headers.common['Authorization'] = token;

    }else{
        alert("로그인이 필요합니다");
        window.location.href = "/main";
        $("#parking-list").append(`<tr><td colspan="6">데이터가 없습니다</td></tr>`)
    }

    //마이페이지 -> 메인페이지 이동
    $('#main-button').click(function () {
        window.location.href="/main";
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
                        $("#existing-car-numbers").append(`
                        <tr>
                            <th scope="row">1</th>
                            <td>AB가1234</td>
                            <td>
                                <div className="form-check">
                                    <input className="form-check-input" type="checkbox" id="car-number-checkbox1">
                                </div>
                            </td>
                        </tr>
                        `);
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

