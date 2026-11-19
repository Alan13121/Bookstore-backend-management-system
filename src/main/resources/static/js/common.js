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
  flashToast('請先登入', 'warning');
  location.href = '/index.html';
  return false;
}

// ============================================================
// UI 互動：提示訊息（toast）、確認對話框、載入中狀態、表單驗證
//
// 為什麼不用 alert() / confirm()：它們會「凍結」整個頁面，必須按掉才能繼續，
// 外觀也無法客製。改用 Bootstrap 的 Toast 與 Modal 元件，訊息會自動消失、
// 不打斷操作。這些元件需要 Bootstrap 的 JS（bootstrap.bundle.min.js），
// 它會建立全域物件 window.bootstrap；萬一 CDN 載入失敗，下面的函式都會
// 退回使用 alert / confirm，功能至少還能用。
// ============================================================

// ---------- Toast 提示訊息 ----------

// type 對應 Bootstrap 的顏色：success / danger / warning / info
const TOAST_ICONS = { success: '✅', danger: '⚠️', warning: '🔔', info: 'ℹ️' };

// 所有 toast 都放進右上角同一個容器，多則訊息會自動往下堆疊
function getToastContainer() {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    document.body.append(container);
  }
  return container;
}

function showToast(message, type = 'success') {
  if (!window.bootstrap) return alert(message);

  const toast = document.createElement('div');
  toast.className = `toast shop-toast shop-toast-${type}`;
  // role="alert" + aria-live：讓螢幕閱讀器也會唸出這則訊息
  toast.setAttribute('role', 'alert');
  toast.setAttribute('aria-live', 'assertive');

  const body = document.createElement('div');
  body.className = 'd-flex align-items-start';
  const text = document.createElement('div');
  text.className = 'toast-body';
  // 用 textContent 放訊息：後端回傳的錯誤訊息不會被當成 HTML 解析（避免 XSS）
  text.textContent = `${TOAST_ICONS[type] ?? ''} ${message}`;
  const close = document.createElement('button');
  close.type = 'button';
  close.className = 'btn-close me-2 mt-2';
  close.setAttribute('data-bs-dismiss', 'toast');
  close.setAttribute('aria-label', '關閉');
  body.append(text, close);
  toast.append(body);

  getToastContainer().append(toast);
  // 錯誤訊息停留久一點，讓使用者有時間讀
  const instance = new bootstrap.Toast(toast, { delay: type === 'danger' ? 6000 : 3000 });
  // 動畫結束後把元素移除，避免 DOM 裡累積一堆隱藏的 toast
  toast.addEventListener('hidden.bs.toast', () => toast.remove());
  instance.show();
}

// 「跨頁」的 toast：例如登入成功後馬上跳轉到首頁，toast 還沒顯示頁面就換掉了。
// 所以先存進 sessionStorage，下一頁載入時再取出顯示（類似後端框架的 flash message）。
const FLASH_KEY = 'flashToast';

function flashToast(message, type = 'success') {
  try {
    sessionStorage.setItem(FLASH_KEY, JSON.stringify({ message, type }));
  } catch (e) {
    // 無痕模式等情況可能無法寫入，就放棄這則提示，不影響主要功能
  }
}

function showPendingFlashToast() {
  try {
    const raw = sessionStorage.getItem(FLASH_KEY);
    if (!raw) return;
    sessionStorage.removeItem(FLASH_KEY);
    const { message, type } = JSON.parse(raw);
    showToast(message, type);
  } catch (e) {
    // 同上，讀不到就算了
  }
}

document.addEventListener('DOMContentLoaded', showPendingFlashToast);

// ---------- 確認對話框 ----------

// 回傳 Promise<boolean>，呼叫端可以這樣寫：
//   if (!(await confirmDialog('確定要刪除嗎？'))) return;
// 和原本 confirm() 的寫法幾乎一樣，只是多了 await。
// 原生 confirm() 會暫停整個 JS 執行；Modal 不會，所以要用 Promise 把「使用者按了哪個鈕」傳回來。
let confirmModalEl = null;

function getConfirmModal() {
  if (confirmModalEl) return confirmModalEl;
  confirmModalEl = document.createElement('div');
  confirmModalEl.className = 'modal fade';
  confirmModalEl.tabIndex = -1;
  confirmModalEl.setAttribute('aria-hidden', 'true');
  // 這段 HTML 是固定的、不含使用者資料，所以可以用 innerHTML；訊息稍後用 textContent 放入
  confirmModalEl.innerHTML = `
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h2 class="modal-title h5" data-role="title"></h2>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="關閉"></button>
        </div>
        <div class="modal-body" data-role="message"></div>
        <div class="modal-footer">
          <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">取消</button>
          <button type="button" class="btn" data-role="confirm"></button>
        </div>
      </div>
    </div>`;
  document.body.append(confirmModalEl);
  return confirmModalEl;
}

function confirmDialog(message, { title = '請確認', confirmText = '確定', danger = true } = {}) {
  if (!window.bootstrap) return Promise.resolve(confirm(message));

  const el = getConfirmModal();
  el.querySelector('[data-role="title"]').textContent = title;
  el.querySelector('[data-role="message"]').textContent = message;
  const confirmBtn = el.querySelector('[data-role="confirm"]');
  confirmBtn.textContent = confirmText;
  confirmBtn.className = `btn ${danger ? 'btn-danger' : 'btn-primary'}`;

  const modal = bootstrap.Modal.getOrCreateInstance(el);
  return new Promise((resolve) => {
    let confirmed = false;
    // 用 onclick 屬性（而不是 addEventListener）：每次開啟會「覆蓋」上一次的處理函式，
    // 不會因為重複開啟對話框而累積多個監聽器
    confirmBtn.onclick = () => {
      confirmed = true;
      modal.hide();
    };
    // 不論按確定、取消、叉叉或 Esc，最後都會觸發 hidden 事件，在這裡統一回傳結果
    el.addEventListener('hidden.bs.modal', () => resolve(confirmed), { once: true });
    modal.show();
  });
}

// ---------- 載入中狀態 ----------

// 在資料容器裡顯示「載入中」轉圈圈，資料回來後 render 函式會把它整個換掉
function showLoading(container, text = '載入中…') {
  // <ul> 裡只能放 <li>，其他容器放 <div>
  const el = document.createElement(container.tagName === 'UL' ? 'li' : 'div');
  el.className = `${container.tagName === 'UL' ? 'list-group-item ' : 'w-100 '}text-center text-secondary py-4`;
  const spinner = document.createElement('span');
  spinner.className = 'spinner-border spinner-border-sm me-2';
  spinner.setAttribute('aria-hidden', 'true');
  el.append(spinner, text);
  container.replaceChildren(el);
}

// 執行非同步工作期間，讓按鈕顯示轉圈圈並停用：
// 1. 使用者知道「有在處理」，不會以為沒反應
// 2. 防止連點造成重複送出（例如新增兩本一樣的書）
// 用 try/finally 確保不論成功或失敗，按鈕最後都會恢復。
async function withLoading(button, task) {
  if (!button) return task();
  if (button.disabled) return undefined; // 已經在處理中，忽略這次點擊

  const originalHtml = button.innerHTML;
  button.disabled = true;
  button.innerHTML =
    '<span class="spinner-border spinner-border-sm me-1" aria-hidden="true"></span>處理中…';
  try {
    return await task();
  } finally {
    // 成功後常會跳頁或重新渲染，按鈕可能已經不在畫面上，恢復它也無妨
    button.disabled = false;
    button.innerHTML = originalHtml;
  }
}

// ---------- 表單驗證 ----------
//
// 表單加上 novalidate 關掉瀏覽器內建的泡泡提示（每個瀏覽器長得不同、也無法調整樣式），
// 改由我們自己檢查，並把錯誤訊息顯示在欄位下方（Bootstrap 的 is-invalid + invalid-feedback）。
// 檢查規則仍然沿用 HTML 上的 required、type="email"、min="0" 等屬性，
// 透過 Constraint Validation API（el.validity）讀出是哪一條規則沒過。

function fieldErrorMessage(el) {
  const v = el.validity;
  if (v.valueMissing) return '此欄位為必填';
  if (v.typeMismatch && el.type === 'email') return 'Email 格式不正確';
  if (v.badInput) return '請輸入數字';
  if (v.rangeUnderflow) return `不可小於 ${el.min}`;
  return el.validationMessage;
}

function setFieldError(input, message) {
  input.classList.add('is-invalid');
  // 錯誤訊息放在欄位後面的 .invalid-feedback；沒有的話就建立一個
  let feedback = input.nextElementSibling;
  if (!feedback?.classList.contains('invalid-feedback')) {
    feedback = document.createElement('div');
    feedback.className = 'invalid-feedback';
    input.after(feedback);
  }
  feedback.textContent = message;
}

function clearFieldError(input) {
  input.classList.remove('is-invalid');
}

// 檢查整個表單：不合格的欄位標紅並顯示原因，游標移到第一個錯誤欄位。
// 回傳 true 代表全部通過，可以送出。
function validateForm(form) {
  let firstInvalid = null;
  form.querySelectorAll('input, select, textarea').forEach((el) => {
    if (el.checkValidity()) {
      clearFieldError(el);
      return;
    }
    setFieldError(el, fieldErrorMessage(el));
    firstInvalid ??= el;
  });
  firstInvalid?.focus();
  return !firstInvalid;
}

// 使用者一開始修改欄位，就把該欄位的錯誤樣式拿掉（事件委派：掛在 document 上，所有頁面通用）
document.addEventListener('input', (e) => {
  if (e.target.classList?.contains('is-invalid')) clearFieldError(e.target);
});

// 綁定表單送出的共用流程：擋下瀏覽器預設送出 → 驗證 → 送出期間按鈕顯示載入中。
// handler 是實際呼叫 API 的 async 函式。
function onFormSubmit(form, handler) {
  form.addEventListener('submit', (e) => {
    e.preventDefault();
    if (!validateForm(form)) return;
    withLoading(form.querySelector('[type="submit"]'), handler);
  });
}
