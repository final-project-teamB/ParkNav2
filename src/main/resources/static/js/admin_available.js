const token = localStorage.getItem('Authorization_admin');
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
    if (token && token !== '') {
        axios.defaults.headers.common['Authorization'] = token;
        $("#login-button").hide();
        $("#logout-button").show();
        $("#main-button").show();
        renderTable();
    } else {
        $("#loginModal").modal('show');
        $("#login-button").show();
        $("#logout-button").hide();
        $("#main-button").hide();
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

    $('#main-button').click(function () {
        window.location.href = "/admin";
    });

});

async function renderTable() {
    const now = new Date();
    let table;
    let theadTime = "<tr><th>시간</th>";

    try {
        const response = await axios.get("/api/mgt/available");
        const data = response.data;

        for (let i = 0; i < 7; i++) {
            const plusDay = new Date(now.getFullYear(), now.getMonth(), now.getDate() + i);
            theadTime += `<th>${plusDay.getFullYear()}-${(plusDay.getMonth() + 1 < 10 ? "0" + (plusDay.getMonth() + 1) : plusDay.getMonth() + 1)}-${(plusDay.getDate() < 10 ? "0" + plusDay.getDate() : plusDay.getDate())}</th>`
        }
        theadTime += "</tr>";

        for (let j = 0; j < 24; j++) {
            table += `<tr><th>${j}시</th>`;
            for (let i = 0; i < 7; i++) {
                const plusDay = new Date(now.getFullYear(), now.getMonth(), now.getDate() + i);
                const match = data.data.parkAvailableDtos.find(function (slot) {
                    return slot.date === `${plusDay.getFullYear()}-${(plusDay.getMonth() + 1 < 10 ? "0" + (plusDay.getMonth() + 1) : plusDay.getMonth() + 1)}-${(plusDay.getDate() < 10 ? "0" + plusDay.getDate() : plusDay.getDate())}` && slot.time === j;
                });
                if (match) {
                    table += match.available === 0 ?`<td>자리 없음</td>` : `<td>${match.available} 구획</td>`;
                } else {
                    table += `<td>${data.data.cmprtCo} 구획</td>`
                }
            }
            table += "</tr>";
        }
        $("#parking-lot-name").text(response.data.data.parkName);
        $("#thead-time").empty().append(theadTime);
        $("#parking-available").append(table);
    } catch (error) {
        console.log(error)
        alert(error.response.data.error.msg);
        return false;
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
