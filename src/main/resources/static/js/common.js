// 將物件轉成格式化字串輸出
function setOutput(content) {
  document.getElementById('output').textContent = JSON.stringify(content, null, 2);
}

// 回傳帶 JWT 的標頭（若有 token）
function authHeader() {
  const token = localStorage.getItem("token"); 
  return token
    ? { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token }
    : { 'Content-Type': 'application/json' };
}

// 公用的帶 header 載入頁面函數
function loadPageWithToken(urlOrOptions, maybeId) {
  let url;
  let id;

  if (typeof urlOrOptions === 'string') {
    url = urlOrOptions;
    id = maybeId;
  } else if (typeof urlOrOptions === 'object') {
    url = urlOrOptions.url;
    id = urlOrOptions.id;
    if (urlOrOptions.params) {
      const params = new URLSearchParams(urlOrOptions.params);
      url += `?${params.toString()}`;
    }
  } else {
    throw new Error('Invalid arguments');
  }

  const token = localStorage.getItem("token");

  fetch(url.split('?')[0], {
    headers: {
      "Authorization": "Bearer " + token
    }
  })
    .then(res => res.text())
    .then(html => {
      history.pushState(null, '', url);
      if (id) {
        html = html.replace(
          '</body>',
          `<script>window.__USER_ID__=${JSON.stringify(id)}</script></body>`
        );
      }
      document.open();
      document.write(html);
      document.close();
    })
    .catch(err => alert(err.message));
}

// 驗證登入狀態，未登入則導回首頁
function checkAuthOrRedirect() {
  const token = localStorage.getItem('token');
  console.log("檢查權限");
  fetch('/api/auth/check', {
    method: 'GET',
    headers: authHeader()
  })
    .then(res => {
      if (!res.ok) {
        alert("未登入帳號");
        throw new Error('未授權');
      }
      return res.json();
    })
    .then(data => {
      console.log("驗證成功，登入者：", data);
    })
    .catch(() => {
      window.location.href = '/index.html';
    });

}

// 嘗試更新 token，失敗就回首頁
async function refreshAndRedirect() {
  try {
    const res = await fetch('/api/auth/refresh-token', {
      method: 'GET',
      headers: authHeader()
    });

    if (!res.ok) {
      alert("Token 更新失敗，將直接跳轉首頁");
      window.location.href = "/index.html";
      return;
    }

    const data = await res.json();
    localStorage.setItem('token', data.token);
    console.log("Token refreshed:", data.token);
    window.location.href = "/index.html";
  } catch (err) {
    console.error("錯誤：", err);
    window.location.href = "/index.html";
  }
}
