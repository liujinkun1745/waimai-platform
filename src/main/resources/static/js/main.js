/**
 * 美味外卖 — 公共 JavaScript
 * 注意：购物车逻辑已移入 merchant-detail.html，实现按商家隔离
 */
document.addEventListener('DOMContentLoaded', function () {
    initNavScroll();
    initAutoDismissAlerts();
});

/* ============ Toast 通知 ============ */
function showToast(message, type, duration) {
    type = type || 'info';
    duration = duration || 3500;
    var container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    var icons = { success: 'bi-check-circle-fill text-success', error: 'bi-exclamation-circle-fill text-danger', info: 'bi-info-circle-fill text-primary' };
    var toast = document.createElement('div');
    toast.className = 'toast-custom ' + type;
    toast.innerHTML =
        '<span class="toast-icon"><i class="bi ' + (icons[type] || icons.info) + '"></i></span>' +
        '<span class="toast-msg">' + message + '</span>' +
        '<span class="toast-close">&times;</span>';
    toast.querySelector('.toast-close').addEventListener('click', function () { removeToast(toast); });
    var timer = setTimeout(function () { removeToast(toast); }, duration);
    toast._timer = timer;
    container.appendChild(toast);
    var all = container.querySelectorAll('.toast-custom');
    while (all.length > 5) { removeToast(all[0]); }
}

function removeToast(toast) {
    if (toast._removing) return;
    toast._removing = true;
    clearTimeout(toast._timer);
    toast.classList.add('removing');
    setTimeout(function () { if (toast.parentNode) toast.parentNode.removeChild(toast); }, 300);
}

/* ============ 确认弹窗 ============ */
function showConfirm(message, onConfirm, title) {
    title = title || '确认操作';
    var id = 'confirmModal_' + Date.now();
    var html =
        '<div class="modal fade" id="' + id + '" tabindex="-1">' +
        '<div class="modal-dialog modal-sm modal-dialog-centered">' +
        '<div class="modal-content">' +
        '<div class="modal-header"><h6 class="modal-title">' + title + '</h6>' +
        '<button type="button" class="btn-close" data-bs-dismiss="modal"></button></div>' +
        '<div class="modal-body"><p class="mb-0">' + message + '</p></div>' +
        '<div class="modal-footer">' +
        '<button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">取消</button>' +
        '<button type="button" class="btn btn-warning btn-sm" id="' + id + '_ok">确认</button>' +
        '</div></div></div></div>';
    var temp = document.createElement('div');
    temp.innerHTML = html;
    document.body.appendChild(temp.firstElementChild);
    var modal = new bootstrap.Modal(document.getElementById(id));
    document.getElementById(id + '_ok').addEventListener('click', function () { modal.hide(); if (onConfirm) onConfirm(); });
    document.getElementById(id).addEventListener('hidden.bs.modal', function () { this.remove(); });
    modal.show();
}

/* ============ 导航栏滚动 ============ */
function initNavScroll() {
    var nav = document.getElementById('mainNav');
    if (!nav) return;
    var ticking = false;
    window.addEventListener('scroll', function () {
        if (!ticking) {
            requestAnimationFrame(function () {
                if (window.scrollY > 50) nav.classList.add('scrolled');
                else nav.classList.remove('scrolled');
                ticking = false;
            });
            ticking = true;
        }
    });
}

/* ============ Alert 自动关闭 ============ */
function initAutoDismissAlerts() {
    setTimeout(function () {
        document.querySelectorAll('.alert-dismissible').forEach(function (el) {
            try { var a = bootstrap.Alert.getOrCreateInstance(el); if (a) a.close(); } catch (e) {}
        });
    }, 4000);
}
