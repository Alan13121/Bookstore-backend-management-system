// 回傳帶 JWT 的標頭（若有 token）
function authHeader() {
  const token = localStorage.getItem("token");
  return {
    'Content-Type': 'application/json',
    ...(token && { 'Authorization': 'Bearer ' + token })
  };
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

// 嘗試更新 token，並回首頁
async function refreshAndRedirect() {
  try {
    const res = await refreshToken();
    window.location.href = "/index.html";
  } catch (err) {
    console.error("錯誤：", err);
    window.location.href = "/index.html";
  }
}

// 嘗試更新 token
async function refreshToken() {
  try {
    const res = await fetch('/api/auth/refresh-token', {
      method: 'GET',
      headers: authHeader()
    });

    if (!res.ok) {
      console.log("Token 更新失敗");
      return null;
    }

    const data = await res.json();
    localStorage.setItem('token', data.token);
    console.log("Token refreshed:", data.token);
    return data.token;
  } catch (err) {
    console.error("錯誤：", err);
    return null;
  }
}

// 解碼 JWT，回傳 payload 物件，失敗回傳 null
function decodeToken(token) {
  if (!token) return null;
  try {
    const payload = token.split('.')[1];
    const decoded = atob(payload);
    return JSON.parse(decoded);
  } catch (e) {
    console.error("解碼 token 失敗", e);
    return null;
  }
}

// 檢查 JWT 是否存在且未過期
function isTokenValid() {
  const token = localStorage.getItem('token');
  if (!token) return false;

  const payload = decodeToken(token);
  if (!payload) return false;

  const now = Math.floor(Date.now() / 1000);
  // exp 大於現在時間表示有效
  return payload.exp && payload.exp > now;
}

// 驗證登入狀態，未登入或過期則導回首頁
function checkAuthOrRedirect() {
  if (!isTokenValid()) {
    // 清掉舊 token
    localStorage.removeItem('token');
    alert("未登入帳號");
    window.location.href = '/index.html';
  } else {
    console.log("Token 有效");
  }
}
