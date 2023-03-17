window.onload = function() {
    const token = localStorage.getItem('Authorization');
    if (token) {
        axios.defaults.headers.common['Authorization'] = token;
    }
};
$('#login-btn').click(function () {
    const userId = $("#username").val();
    const password = $("#password").val();



    // axios.post("/api/users/login", body,{withCredentials:true})
    //     .then(response => {
    //         const data = response.data;
    //         const token = response.headers.authorization;
    //
    //         console.log(token);
    //         axios.defaults.headers.common['Authorization'] = response.headers.authorization;
    //         localStorage.setItem('Authorization', token);
    //         document.cookie = `Authorization=${token}`;
    //         window.location.href = '/main';
    //
    //     })
    //     .catch(error => {
    //         console.log(error);
    //         alert(error.response.data.error.msg)
    //         return false;
    //     });

    // axios.post('/api/users/login',body)
    //     .then(response => {
    //     // 로그인 성공 시 토큰 값을 localStorage에 저장
    //     const token = response.headers.authorization;
    //
    //     localStorage.setItem('Authorization', token);
    //     document.cookie = `Authorization=${token}`;
    //     axios.defaults.headers.common['Authorization'] = localStorage.getItem('Authorization');
    //         // main 페이지로 이동
    //     // window.location.href = '/main';
    //         window.location.replace("/main");
    //     }).catch(error => {
    //     console.log(error);
    //     alert('로그인에 실패하셨습니다. 다시 로그인해 주세요.');
    //     window.location.reload();
    // });

    const body = {
        userId: userId,
        password: password,
    };


    axios.post('/api/users/login', body,{withCredentials: true})
        .then(response => {
            // 로그인 성공 시 토큰 값을 localStorage에 저장
            const token = response.headers.authorization;
            localStorage.setItem('Authorization', token);
            document.cookie = `Authorization=${token}`;
            // 다음 페이지로 요청을 보낼 때 헤더에 토큰을 추가
            axios.defaults.headers.common['Authorization'] = token;

            // 다음 페이지로 이동
            window.location.replace('/main', {
                headers: { Authorization: token },
            });
            //     window.location.href = url;

        })
        .catch(error => {
            console.log(error);
            alert('로그인에 실패하셨습니다. 다시 로그인해 주세요.');
            window.location.reload();
        });

    // axios.post("/api/users/login", body, {
    //     headers: {
    //         'Content-Type': 'application/json'
    //     }
    // }).then(response => {
    //     // if (response.data === 'success') {
    //     let host = window.location.host;
    //     let url = `http://${host}/main`;
    //     const token = response.headers.authorization;
    //     axios.defaults.headers.common['Authorization'] = token;
    //     localStorage.setItem('Authorization', token);
    //     document.cookie = `Authorization=${token}`;
    //     window.location.href = url;
    //     // } else {
    //     //     alert('로그인에 실패하셨습니다. 다시 로그인해 주세요.');
    //     //     window.location.reload();
    //     // }
    // }).catch(error => {
    //     console.log(error);
    //     alert('로그인에 실패하셨습니다. 다시 로그인해 주세요.');
    //     window.location.reload();
    // });

});