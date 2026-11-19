// 書籍編輯頁：從網址取得 id，載入資料填入表單，送出時以 PUT 更新

const BOOK_FORM_FIELDS = {
  title: 'book-title',
  author: 'book-author',
  description: 'book-desc',
  listPrice: 'book-list-price',
  salePrice: 'book-sale-price',
};

function fillBookForm(book) {
  document.getElementById('book-id').textContent = book.id;
  Object.entries(BOOK_FORM_FIELDS).forEach(([key, id]) => {
    document.getElementById(id).value = book[key] ?? '';
  });
}

async function updateBook(id) {
  const fields = readFields(BOOK_FORM_FIELDS);
  const book = {
    ...fields,
    listPrice: toNumberOrNull(fields.listPrice),
    salePrice: toNumberOrNull(fields.salePrice),
  };

  try {
    await apiFetch(`/api/books/${id}`, { method: 'PUT', body: book });
    flashToast('書籍已更新');
    location.href = '/book/books.html';
  } catch (err) {
    showToast(`更新失敗：${err.message}`, 'danger');
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  if (!checkAuthOrRedirect()) return;

  const id = getQueryParam('id');
  if (!id) {
    flashToast('缺少書籍 ID', 'warning');
    location.href = '/book/books.html';
    return;
  }

  const form = document.getElementById('edit-book-form');
  // 資料載入前先停用整個表單（<fieldset disabled> 會一併停用裡面所有欄位與按鈕），
  // 避免使用者在資料還沒回來時就按下保存，送出空白內容
  const fieldset = form.querySelector('fieldset');
  fieldset.disabled = true;

  try {
    fillBookForm(await apiFetch(`/api/books/${id}`));
  } catch (err) {
    flashToast(`載入書籍失敗：${err.message}`, 'danger');
    location.href = '/book/books.html';
    return;
  }
  fieldset.disabled = false;

  onFormSubmit(form, () => updateBook(id));
});
