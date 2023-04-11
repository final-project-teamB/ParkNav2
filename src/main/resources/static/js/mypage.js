const token = localStorage.getItem('Authorization');
let currentPage = 0;
const pageSize = 10; // 한 페이지당 보여줄 항목 수
let totalPages = 0;
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

    $("#parking-list").empty();
    if (token && token !== '') {
        axios.defaults.headers.common['Authorization'] = token;
        fetchData(0);
    } else {
        alert("로그인이 필요합니다");
        window.location.href = "/main";
        $("#parking-list").append(`<tr><td colspan="6">데이터가 없습니다</td></tr>`)
    }

    //마이페이지 -> 메인페이지 이동
    $('#main-button').click(function () {
        window.location.href = "/main";
    });

    //차량등록 버튼 클릭 이벤트
    $('#register-button').click(function () {
        myCarList();
    });

    //라디오버튼 이벤트
    $(document).on('click', 'input[type="radio"]', function () {
        $('input[type="radio"]').not(this).prop('checked', false);
        const carNum = $(this).closest('tr').find('td:nth-child(2)').text();
        axios.put("/api/car/rep", {carNum: carNum})
            .then(response => {
                alert(response.data.msg);
                myCarList();
            })
            .catch(error => {
                console.log(error.response.data.error.msg);
                return false;
            });
    });

    //삭제버튼 클릭 이벤트
    $('#existing-car-numbers').on('click', '[name=car-delete-button]', function () {
        const carNum = $(this).closest('tr').find('td:eq(1)').text();
        if (confirm(carNum + " 차량을 삭제하시겠습니까?")) {
            axios.delete("/api/car/reg", {data: {carNum: carNum}})
                .then(response => {
                    alert(response.data.msg);
                    myCarList();
                })
                .catch(error => {
                    console.log(error.response.data.error.msg);
                    return false;
                });
        }
    });

    //모달 차량번호 등록
    $('#car-number-confirm').click(function () {
        myCarAdd();
    });

    //모달 차량번호박스 엔터
    $('#car-number-input').keypress(function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            myCarAdd();
        }
    });

});

function myCarAdd(){
    const carnum = $("#car-number-input").val();
    const pattern1 = /^[0-9]{2,3}[가-힣]{1}[0-9]{4}$/;
    const pattern2 = /^[가-힣]{2}[0-9]{2}[가-힣]{1}[0-9]{4}$/;
    if (pattern1.test(carnum) || pattern2.test(carnum)) {
        axios.post(`/api/car/reg`, {carNum: carnum})
            .then(response => {
                if (response.data.msg === "차량 등록 성공") {
                    alert(response.data.msg);
                    myCarList();
                    $("#car-number-input").val("");
                } else {
                    alert("등록에 실패했습니다.")
                    return false;
                }
            })
            .catch(error => {
                alert(error.response.data.error.msg);
                return false;
            });
    } else {
        alert("올바른 차량번호 형식이 아닙니다.");
    }
}

//나의 차량 목록
function myCarList() {
    $("#existing-car-numbers").empty();
    axios.get("/api/car/check")
        .then(response => {
            const data = response.data.data;
            let num = 1;
            data.map((item) => {
                $("#existing-car-numbers").append(`
                    <tr>
                        <td>${num++}</td>
                        <td>${item.carNum}</td>
                        <td>
                        <div class="form-check">
                            <input class="form-check-input" type="radio" ${num === 2 ? "checked" : null}>
                        </div>
                        </td>
                        <td><button class="btn btn-outline-danger mx-2" name="car-delete-button">삭제</button></td>
                    </tr>`
                );
            })
            if (data.length == 0) {
                $("#existing-car-numbers").append(`<tr><td colspan="4">등록된 차량이 없습니다</td></tr>`)
            }
        })
        .catch(error => {
            console.log(error.response.data.error.msg);
            return false;
        });
}

// 예약확인 버튼 클릭 시 모달창 띄우기
function viewReservation(id, startDate, endDate) {
    $('#reservation-modal').modal('show');
    const dateFormat = 'YYYY-MM-DDTHH:mm:ss';
    const body = {
        startDate: moment(startDate, 'YYYY-MM-DD HH:mm').format(dateFormat),
        endDate: moment(endDate, 'YYYY-MM-DD HH:mm').format(dateFormat)
    };
    const params = new URLSearchParams(body).toString();
    axios.get(`/api/booking/${id}?${params}`)
        .then(response => {
            console.log(response)
            const data = response.data;
            $("#parking-lot-available-modal").attr("value", data.data.available);
            $("#parking-lot-booking-modal").attr("value", data.data.booking);
        })
        .catch(error => {
            alert(error.response.data.error.msg);
            return false;
        });
}

// 예약 취소 버튼 클릭시 이벤트
function cancelReservation(id) {
    if (confirm("예약을 취소 하시겠습니까?")) {
        axios.delete(`/api/booking/${id}`)
            .then(response => {
                const data = response.data;
                alert(data.msg)
                fetchData(currentPage);
            })
            .catch(error => {
                alert(error.response.data.error.msg);
                return false;
            });
    }
}

//예약 목록 불러오기
function bookingList() {
    $("#parking-list").empty();
    axios.get("/api/booking/mypage")
        .then(response => {
            const data = response.data.data;
            data.map((item) => {
                let button;
                if (item.status === "UNUSED") {
                    button = `<button type="button" class="btn btn-outline-secondary btn-sm mx-1" onclick="viewReservation(${item.parkId},'${item.startDate}','${item.endDate}')">예약 확인</button>` +
                        `<button type="button" class="btn btn-outline-warning btn-sm mx-1" onclick="cancelReservation(${item.bookingId})">예약 취소</button>`;
                } else if (item.status === "EXPIRED") {
                    button = `<button type="button" class="btn btn-outline-danger btn-sm mx-1">기간만료</button>`;
                } else {
                    button = `<button type="button" class="btn btn-outline-success btn-sm mx-1">사용완료</button>`;
                }
                $("#parking-list").append(`
                    <tr>
                        <td>${item.parkName}</td>
                        <td>${item.carNum}</td>
                        <td>${item.startDate} ~<br> ${item.endDate}</td>
                        <td>${item.charge + "원"}</td>
                        <td>
                            ${button}
                        </td>
                    </tr>`
                );
            })
            if (data.length == 0) {
                $("#parking-list").append(`<tr><td colspan="5">데이터가 없습니다</td></tr>`)
            }
        })
        .catch(error => {
            alert(error.response.data.error.msg);
            return false;
        });
}

function fetchData(page) {
    const body = {
        page: page,
        size: pageSize
    };
    const params = new URLSearchParams(body).toString();
    axios.get(`/api/booking/mypage?${params}`)
        .then(response => {
            const data = response.data.data.content;
            const content = data.content;
            let num = 1;
            $("#parking-list").empty();
            data.map((item) => {
                let button;
                if (item.status === "UNUSED") {
                    button = `<button type="button" class="btn btn-outline-warning btn-sm mx-1" onclick="cancelReservation(${item.bookingId})">예약 취소</button>`;
                } else if (item.status === "EXPIRED") {
                    button = `<button type="button" class="btn btn-outline-danger btn-sm mx-1">기간만료</button>`;
                } else {
                    button = `<button type="button" class="btn btn-outline-success btn-sm mx-1">사용완료</button>`;
                }
                $("#parking-list").append(`
                    <tr>
                        <td>${item.parkName}</td>
                        <td>${item.carNum}</td>
                        <td>${item.startDate} ~<br> ${item.endDate}</td>
                        <td>${item.charge + "원"}</td>
                        <td>
                            ${button}
                        </td>
                    </tr>`
                );
            });
            // 현재 페이지 번호 설정
            currentPage = page;
            // 총 페이지 수 설정
            totalPages = response.data.data.totalPages;
            // 페이징 버튼 생성 함수 호출
            renderPagination();
        })
        .catch(error => {
            console.log(error);
        });
}

function renderPagination() {
    const pagination = $(".pagination");
    pagination.empty();
    if (totalPages > 0) {
        // 이전 페이지 버튼 추가
        pagination.append(`
            <li class="page-item" id="previous-page">
                <a class="page-link" href="#">이전</a>
            </li>
        `);

        // 페이지 번호 버튼 추가
        const startPage = Math.max(0, currentPage - 2);
        const endPage = Math.min(totalPages - 1, currentPage + 2);
        for (let i = startPage; i <= endPage; i++) {
            const activeClass = currentPage === i ? "active" : "";
            pagination.append(`
                <li class="page-item ${activeClass}">
                    <a class="page-link page-num" href="#" data-page="${i}">${i + 1}</a>
                </li>
            `);
        }

        // 다음 페이지 버튼 추가
        pagination.append(`
            <li class="page-item" id="next-page">
                <a class="page-link" href="#">다음</a>
            </li>
        `);

        // 이전 페이지 버튼 클릭 이벤트
        $("#previous-page").click(() => {
            if (currentPage > 0) {
                fetchData(currentPage - 1);
            }
        });

        // 다음 페이지 버튼 클릭 이벤트
        $("#next-page").click(() => {
            if (currentPage < totalPages - 1) {
                fetchData(currentPage + 1);
            }
        });

        // 페이지 번호 버튼 클릭 이벤트
        $(".page-num").click((event) => {
            const page = $(event.target).data("page");
            fetchData(page);
        });
    }
}
