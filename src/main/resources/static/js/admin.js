const token = localStorage.getItem('Authorization_admin');
let currentPage = 0;
const pageSize = 10; // 한 페이지당 보여줄 항목 수
let totalPages = 0;
let parkId;
$(document).ready(function () {
    axios.interceptors.response.use(function (response) {
        // 응답 성공 직전 호출되는 콜백
        return response;
    }, function (error) {
        // 응답 에러 직전 호출되는 콜백
        const errorMsg = error.response.data?.error?.msg;
        if (errorMsg === "토큰이 유효하지 않습니다." || errorMsg === "토큰이 없습니다." || errorMsg === undefined) {
            alert("로그인이 만료 되었습니다 재 로그인 해주세요")
            localStorage.removeItem("Authorization_admin");
            window.location.reload();
            return false;
        }
        return Promise.reject(error);
    });

    $("#parking-list").empty();
    if (token && token !== '') {
        axios.defaults.headers.common['Authorization'] = token;
        $("#login-button").hide();
        $("#enter-button").show();
        $("#logout-button").show();
        $("#available-button-button").show();
        fetchData(0);
    } else {
        $("#loginModal").modal('show');
        $("#login-button").show();
        $("#enter-button").show();
        $("#logout-button").hide();
        $("#available-button-button").hide();
        $("#parking-list").append(`<tr><td colspan="8">데이터가 없습니다</td></tr>`)
    }

    $('#logout-button').click(function () {
        if (confirm("로그아웃 하시겠습니까?")) {
            // 로컬 스토리지에서 토큰 삭제
            localStorage.removeItem("Authorization_admin");
            window.location.reload();
        } else {
            return false;
        }
    });

    $('#login-btn').click(function () {
        adminLogin();
    });

    //모달 비밀번호에서 Enter 키 입력 시 이벤트
    $('#password').keypress(function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            adminLogin();
        }
    });

    //모달 아이디에서 Enter 키 입력 시 이벤트
    $('#username').keypress(function (event) {
        if (event.keyCode === 13) {
            event.preventDefault();
            $('#password').focus();
        }
    });

    $('#available-button').click(function () {
        window.location.href = "/admin/available";
    });

    //모달 아이디에서 Enter 키 입력 시 이벤트
    $('#enter-btn').click(function () {
        let carNum = $("#enterCarNum").val();
        let parkingTime = $("#enterParkTime").val();
        if (carNum ===""){
            alert("차량 번호를 입력해주세요");
            return false;
        }
        if (parkingTime > 168) {
            alert("입차 최대 가능시간은 168시간입니다");
            return false
        }
        $("#enterCarNum").val("");
        $("#enterParkTime").val("");
        body = {
            parkId: parkId,
            carNum: carNum,
            parkingTime: parkingTime
        }
        axios.post(`/api/mgt/enter`, body)
            .then(response => {
                alert(response.data.msg);
                $('#enterModal').modal('hide');

                fetchData(currentPage);
            })
            .catch(error => {
                alert(error.response.data.error.msg);
                return false;
            });
    });

});

function carEnter(parkId, carNum) {
    if (confirm(carNum + " 차량을 입차 하시겠습니까?")) {
        body = {
            parkId: parkId,
            carNum: carNum
        }
        axios.post("/api/mgt/enter", body)
            .then(response => {
                alert(response.data.msg);
                fetchData(currentPage);
            })
            .catch(error => {
                alert(error.response.data.error.msg)
                return false;
            });
    }
}

function carExit(parkId, carNum) {
    if (confirm(carNum + " 차량을 출차 하시겠습니까?")) {
        body = {
            parkId: parkId,
            carNum: carNum
        }
        axios.put("/api/mgt/exit", body)
            .then(response => {
                alert(response.data.msg);
                fetchData(currentPage);
            })
            .catch(error => {
                alert(error.response.data.error.msg)
                return false;
            });
    }
}

function adminLogin() {
    let userId = $("#username").val();
    let password = $("#password").val();
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

    axios.post("/api/admins/login", body)
        .then(response => {
            if (response.data.msg === '로그인이 완료되었습니다.') {
                const token = response.headers.authorization;
                axios.defaults.headers.common['Authorization'] = response.headers.authorization;
                localStorage.setItem('Authorization_admin', token);
                window.location.reload();
            } else {
                alert('로그인에 실패하셨습니다. 다시 로그인해 주세요.')
                return false;
            }
        })
        .catch(error => {
            alert(error.response.data.error.msg)
            return false;
        });
}

function fetchData(page) {
    const body = {
        page: page,
        size: pageSize
    };
    const params = new URLSearchParams(body).toString();
    axios.get(`/api/mgt/check?${params}`)
        .then(response => {
            const data = response.data.data.page.content;
            parkId = response.data.data.parkId;
            let num = 1;
            $("#parking-lot-name").text(response.data.data.parkName);
            $("#actual-charge").text(response.data.data.totalActualCharge);
            $("#estimated-charge").text(response.data.data.totalEstimatedCharge);
            $("#parking-list").empty();
            data.map((item) => {
                const buttons = document.querySelector(".btn-outline-success");
                let button;
                if (item.exitTime == null) {
                    if (item.enterTime == null ) {
                        if (new Date(item.bookingStartTime) <= new Date() && new Date(item.bookingEndTime) > new Date()) {
                            button = `<button type="button" class="btn btn-outline-warning btn-sm mx-1" onclick="carEnter('${parkId}','${item.carNum}')">입차하기</button>`;
                        } else {
                            button = `<button type="button" class="btn btn-outline-secondary btn-sm mx-1">예약차량</button>`;
                        }
                    } else {
                        button = `<button type="button" class="btn btn-outline-info btn-sm mx-1" onclick="carExit('${parkId}','${item.carNum}')">출차하기</button>`;
                    }
                    if (buttons.innerText !== "출차완료" && new Date(item.bookingEndTime) < new Date() && item.enterTime == null) {
                        button = `<button type="button" class="btn btn-outline-danger btn-sm mx-1">예약만료</button>`;
                    }
                }else
                {
                    button = `<button type="button" class="btn btn-outline-success btn-sm mx-1">출차완료</button>`;
                }
                $("#parking-list").append(`
                <tr ${new Date(item.bookingEndTime) < new Date() && item.exitTime == null && item.enterTime != null ? 'style="color: red;"' : ''}>
                    <td>${(pageSize * (page)) + num++}</td>
                    <td>${item.carNum}</td>
                    <td>${item.enterTime == null ? "-" : item.enterTime}</td>
                    <td>${item.exitTime == null ? "-" : item.exitTime}</td>
                    <td>${item.charge + "원"}</td>
                    <td>${item.bookingStartTime}</td>
                    <td>${item.bookingEndTime}</td>
                    <td>${item.exitTime == null && item.enterTime == null ? "예약" : item.exitTime == null ? "주차" : "출차"}</td>
                    <td>${button}</td> 
                </tr>
                `);
            });
            currentPage = page; // 현재 페이지 번호 설정
            totalPages = response.data.data.page.totalPages; // 총 페이지 수 설정
            renderPagination(); // 페이징 버튼 생성 함수 호출
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
