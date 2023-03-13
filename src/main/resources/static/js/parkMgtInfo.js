function showText(buttonId, textId) {
    // 해당 버튼을 숨김
    document.getElementById(buttonId).style.display = "none";
    // 해당 텍스트를 보여줌
    document.getElementById(textId).style.display = "block";
}

function updateExitTime(btnId) {
    const row = document.querySelector(`#${btnId}`).closest('tr');
    const now = new Date();
    const year = now.getFullYear().toString().padStart(4, '0');
    const month = (now.getMonth()+1).toString().padStart(2, '0');
    const day = now.getDate().toString().padStart(2, '0');
    const hours = now.getHours().toString().padStart(2, '0');
    const minutes = now.getMinutes().toString().padStart(2, '0');
    const seconds = now.getSeconds().toString().padStart(2, '0');
    const exitTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    row.cells[3].textContent = exitTime;
}

document.querySelector('#btn1').addEventListener('click', () => {
    updateExitTime('btn1');
});

document.querySelector('#btn2').addEventListener('click', () => {
    updateExitTime('btn2');
});
