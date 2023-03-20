const token = localStorage.getItem('Authorization_admin');

$(document).ready(function () {
    axios.interceptors.response.use(function (response) {
        // 응답 성공 직전 호출되는 콜백
        return response;
    }, function (error) {
        // 응답 에러 직전 호출되는 콜백
        const errorMsg = error.response.data?.error?.msg;
        console.log(errorMsg);
        if (errorMsg === "토큰이 유효하지 않습니다." || errorMsg === "토큰이 없습니다." || errorMsg === undefined) {
            alert("로그인이 만료 되었습니다 재 로그인 해주세요")
            localStorage.removeItem("Authorization_admin");
            window.location.reload();
        }
        return Promise.reject(error);
    });

    localStorage.removeItem("Authorization");
    $("#parking-list").empty();
    if (token && token !== '') {
        axios.defaults.headers.common['Authorization'] = token;
        $("#login-button").hide();
        $("#logout-button").show();

        axios.get("/api/mgt/check")
            .then(response => {
                const data = response.data.data;
                console.log(data);
                let num = 1;
                data.map((item)=>{
                    $("#parking-list").append(`
                    <tr>
                        <td>${num++}</td>
                        <td>${item.carNum}</td>
                        <td>${item.enterTime}</td>
                        <td>${item.exitTime==null?"-":item.exitTime}</td>
                        <td>${item.exitTime==null?"-":item.charge+"원"}</td>
                        <td>${item.exitTime==null?"주차":"출차"}</td>
                    </tr>`
                    );
                })
                if(data.length==0){
                    $("#parking-list").append(`<tr><td colspan="6">데이터가 없습니다</td></tr>`)
                }
            })
            .catch(error => {
                console.log(error.response.data.error.msg)
                return false;
            });

    }else{
        $("#loginModal").modal('show');
        $("#login-button").show();
        $("#logout-button").hide();
        $("#parking-list").append(`<tr><td colspan="6">데이터가 없습니다</td></tr>`)
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
        const userId = $("#username").val();
        const password = $("#password").val();
        const body = {
            userId: userId,
            password: password,
        };

        axios.post("/api/admins/login", body)
            .then(response => {
                console.log(response)
                if(response.data.msg === '로그인이 완료되었습니다.') {
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
    });

});

