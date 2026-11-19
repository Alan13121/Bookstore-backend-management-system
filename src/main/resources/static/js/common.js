// ============================================================
// 共用工具：每個頁面都會先載入這支檔案，再載入自己的 JS。
// 這裡集中處理「Token 存取」、「呼叫 API」、「登入檢查」等重複邏輯，
// 各頁面只需要專注在自己的畫面與資料。
// ============================================================

const TOKEN_KEY = 'token';

// ---------- Token 存取 ----------

function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

// 回傳帶 JWT 的標頭（若有 token）。
// 後端的 JwtAuthenticationFilter 會從 Authorization: Bearer xxx 取出 token 驗證身分。
function authHeader() {
  const token = getToken();
  return {
    'Content-Type': 'application/json',
    ...(token && { Authorization: 'Bearer ' + token }),
  };
}

// ---------- 呼叫 API ----------

// 取出失敗回應中的錯誤訊息。
// 後端 GlobalExceptionHandler 回傳 {status, message}；
// 但被 Filter 擋下的請求（例如 DynamicAuthorizationFilter 的 403）回的是純文字，
// 所以先讀成文字再嘗試解析 JSON，兩種格式都能取到訊息。
async function readErrorMessage(res, fallback) {
  try {
    const text = await res.text();
    if (!text) return fallback;
    try {
      return JSON.parse(text).message || fallback;
    } catch (e) {
      return text.length <= 200 ? text : fallback;
    }
  } catch (e) {
    return fallback;
  }
}

// 統一的 API 呼叫方式：
// - 自動帶上 JWT 與 Content-Type
// - body 傳入物件即可，會自動轉成 JSON
// - 非 2xx 時丟出 Error（message 為後端回傳的錯誤訊息），呼叫端用 try/catch 處理
// - 204 No Content（例如刪除成功）回傳 null
async function apiFetch(url, { method = 'GET', body } = {}) {
  const res = await fetch(url, {
    method,
    headers: authHeader(),
    body: body === undefined ? undefined : JSON.stringify(body),
  });

  if (!res.ok) {
    const error = new Error(await readErrorMessage(res, `請求失敗（${res.status}）`));
    error.status = res.status;
    throw error;
  }

  if (res.status === 204) return null;
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

// ---------- 其他小工具 ----------

// 跳脫 HTML 特殊字元，避免以 innerHTML 組字串時被注入（XSS）
function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

// 讀取網址上的查詢參數，例如 book.html?id=3 → getQueryParam('id') === '3'
function getQueryParam(name) {
  return new URLSearchParams(location.search).get(name);
}

// 把頁面上一組欄位一次讀出來：{ title: 'book-title' } → { title: <#book-title 的值> }
function readFields(fieldIds) {
  return Object.fromEntries(
    Object.entries(fieldIds).map(([key, id]) => [key, document.getElementById(id).value]),
  );
}

// 輸入框的空字串轉成 null，數字欄位才不會變成 NaN 傳給後端
function toNumberOrNull(value) {
  return value === '' ? null : Number(value);
}

// ---------- 角色勾選框（user.html、users.html、roles.html 共用） ----------

// 角色的中文顯示名稱；資料庫的角色名稱不含 ROLE_ 前綴，沒列在這裡的角色直接顯示原名
const ROLE_LABELS = { ADMIN: '管理員', STAFF: '員工', WORKER: '作業員' };

function roleLabel(name) {
  return ROLE_LABELS[name] ? `${ROLE_LABELS[name]}（${name}）` : name;
}

// 在 container 內為每個角色產生一個勾選框，checkedNames 中的角色預設勾選。
// 用 createElement 而不是組 HTML 字串，角色名稱就不需要另外跳脫。
// 套用 Bootstrap 的 form-check 結構：<div class="form-check"><input><label for></div>，
// label 的 for 指向 input 的 id，點文字也能勾選。id 需全頁唯一，
// 而 roles.html 會為每個用戶各畫一組，所以用計數器產生不重複的 id。
let roleCheckboxSeq = 0;

function renderRoleCheckboxes(container, roles, checkedNames = []) {
  container.innerHTML = '';
  roles.forEach((role) => {
    const wrapper = document.createElement('div');
    wrapper.className = 'form-check form-check-inline';

    const checkbox = document.createElement('input');
    checkbox.type = 'checkbox';
    checkbox.className = 'form-check-input';
    checkbox.id = `role-checkbox-${++roleCheckboxSeq}`;
    checkbox.value = role.id;
    checkbox.checked = checkedNames.includes(role.name);

    const label = document.createElement('label');
    label.className = 'form-check-label';
    label.htmlFor = checkbox.id;
    label.textContent = roleLabel(role.name);

    wrapper.append(checkbox, label);
    container.append(wrapper);
  });
}

// 取出 container 內被勾選的角色 id，對應後端 DTO 的 roleIds
function getCheckedRoleIds(container) {
  return Array.from(container.querySelectorAll('input[type="checkbox"]:checked')).map((cb) =>
    Number(cb.value),
  );
}

// ---------- JWT 與登入狀態 ----------

// 嘗試更新 token（延長登入時效），失敗回傳 null
async function refreshToken() {
  if (!getToken()) return null;
  try {
    const data = await apiFetch('/api/auth/refresh-token');
    setToken(data.token);
    return data.token;
  } catch (err) {
    // 不印出 token 本身，避免敏感資料出現在 console
    console.warn('Token 更新失敗：', err.message);
    return null;
  }
}

// 解碼 JWT 的 payload（不驗證簽章，只用來讀 exp、roles 等資訊顯示畫面），失敗回傳 null。
// 真正的驗證由後端負責：前端解出來的內容只能拿來決定「顯示什麼」，不能拿來當作授權依據。
function decodeToken(token) {
  if (!token) return null;
  try {
    // JWT 使用 base64url（- 與 _），atob 只認得標準 base64（+ 與 /），要先轉換
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const bytes = Uint8Array.from(atob(base64), (c) => c.charCodeAt(0));
    // 以 UTF-8 解碼，payload 內有中文時才不會變亂碼
    return JSON.parse(new TextDecoder().decode(bytes));
  } catch (e) {
    console.error('解碼 token 失敗', e);
    return null;
  }
}

// 檢查 JWT 是否存在且未過期
function isTokenValid() {
  const payload = decodeToken(getToken());
  if (!payload || !payload.exp) return false;
  // JWT 的 exp 單位是秒，Date.now() 是毫秒
  return payload.exp > Math.floor(Date.now() / 1000);
}

// 需要登入的頁面在載入時呼叫：未登入或過期則導回首頁並回傳 false。
// 呼叫端看到 false 就應該停止後續動作，避免在跳轉前還送出一堆會 401 的請求。
function checkAuthOrRedirect() {
  if (isTokenValid()) return true;
  clearToken();
  alert('未登入帳號');
  location.href = '/index.html';
  return false;
}
