/**
 * pageUtils.js — Shared page state helpers
 *
 * Provides: api(), showLoading(), showEmpty(), showError(), showContent()
 * Used by every page fragment to handle loading / empty / error / normal states.
 */

/* ── API helper ── wraps fetch with JSON parsing and error handling */


/* ── State renderer ── */

/**
 * showLoading(container, rows = 5, type = 'table')
 * Renders animated skeleton placeholders inside container.
 * type: 'table' | 'cards' | 'list'
 */
(function () {
    function skeletonLine(w) {
        w = w || '100%';
        return (
            '<div style="height:14px;background:var(--border-light);' +
            'border-radius:4px;width:' +
            w +
            ';animation:skeleton-pulse 1.4s ease-in-out infinite"></div>'
        );
    }

    function makeArray(n) {
        return Array.from({ length: n });
    }

    function join(items) {
        return items.join('');
    }

    window.api = function (path, opts) {
        opts = opts || {};
        return new Promise(function (resolve, reject) {
            var fetchOpts = {
                headers: {
                    'Content-Type': 'application/json'
                }
            };
            // copy opts into fetchOpts (no spread)
            for (var key in opts) {
                if (opts.hasOwnProperty(key)) {
                    fetchOpts[key] = opts[key];
                }
            }
            // handle body safely
            if (opts.body) {
                fetchOpts.body = JSON.stringify(opts.body);
            }
            fetch('/api/' + path, fetchOpts).then(function (res) {
                if (!res.ok) {
                    reject(new Error('API error ' + res.status));
                    return;
                }
                return res.json();
            }).then(function (data) {
                resolve(data);
            }).catch(function (err) {
                reject(err);
            });
        });
    };

    window.showLoading = function (container, rows, type) {
        rows = rows || 5;
        type = type || 'table';

        var html = '';

        if (type === 'table') {
            html =
                '<div style="border:1px solid var(--border);border-radius:var(--radius);overflow:hidden">' +
                join(
                    makeArray(rows).map(function () {
                        return (
                            '<div style="display:flex;align-items:center;gap:16px;padding:14px 20px;border-bottom:1px solid var(--border-light)">' +
                            '<div style="width:36px;height:36px;border-radius:50%;background:var(--border-light);flex-shrink:0;animation:skeleton-pulse 1.4s ease-in-out infinite"></div>' +
                            '<div style="flex:1;display:flex;flex-direction:column;gap:6px">' +
                            skeletonLine('60%') +
                            skeletonLine('40%') +
                            '</div>' +
                            '<div style="width:80px">' +
                            skeletonLine() +
                            '</div>' +
                            '<div style="width:60px">' +
                            skeletonLine() +
                            '</div>' +
                            '</div>'
                        );
                    })
                ) +
                '</div>';
        } else if (type === 'cards') {
            html =
                '<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(260px,1fr));gap:16px">' +
                join(
                    makeArray(rows).map(function () {
                        return (
                            '<div style="border:1px solid var(--border);border-radius:var(--radius);padding:20px;display:flex;flex-direction:column;gap:10px">' +
                            '<div style="display:flex;align-items:center;gap:12px">' +
                            '<div style="width:44px;height:44px;border-radius:var(--radius-sm);background:var(--border-light);flex-shrink:0;animation:skeleton-pulse 1.4s ease-in-out infinite"></div>' +
                            '<div style="flex:1;display:flex;flex-direction:column;gap:6px">' +
                            skeletonLine('70%') +
                            skeletonLine('50%') +
                            '</div>' +
                            '</div>' +
                            skeletonLine() +
                            skeletonLine('80%') +
                            '</div>'
                        );
                    })
                ) +
                '</div>';
        } else if (type === 'list') {
            html =
                '<div style="display:flex;flex-direction:column;gap:10px">' +
                join(
                    makeArray(rows).map(function () {
                        return (
                            '<div style="display:flex;align-items:center;gap:12px;padding:12px 0;border-bottom:1px solid var(--border-light)">' +
                            '<div style="width:40px;height:40px;border-radius:50%;background:var(--border-light);flex-shrink:0;animation:skeleton-pulse 1.4s ease-in-out infinite"></div>' +
                            '<div style="flex:1;display:flex;flex-direction:column;gap:6px">' +
                            skeletonLine('55%') +
                            skeletonLine('35%') +
                            '</div>' +
                            '</div>'
                        );
                    })
                ) +
                '</div>';
        }

        container.innerHTML = html;
    };

    window.showEmpty = function (container, opts) {
        opts = opts || {};

        var icon = opts.icon || '#icon-search';
        var title = opts.title || 'Nothing here yet';
        var message = opts.message || '';
        var actionLabel = opts.actionLabel || '';
        var onAction = opts.onAction || null;

        var html =
            '<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;' +
            'padding:60px 20px;text-align:center;color:var(--text-3);gap:12px">' +
            '<svg width="40" height="40" style="opacity:.3;color:var(--text-2)">' +
            '<use href="' + icon + '"/>' +
            '</svg>' +
            '<div style="font-size:15px;font-weight:600;color:var(--text-2)">' +
            title +
            '</div>' +
            (message
                ? '<div style="font-size:13px;max-width:300px;line-height:1.6">' +
                message +
                '</div>'
                : '') +
            (actionLabel && onAction
                ? '<button class="btn btn-primary" style="margin-top:8px" onclick="(' +
                onAction.toString() +
                ')()">' +
                actionLabel +
                '</button>'
                : '') +
            '</div>';

        container.innerHTML = html;
    };

    window.showError = function (container, opts) {
        opts = opts || {};

        var message = opts.message || 'Something went wrong loading this data.';
        var onRetry = opts.onRetry || null;

        var html =
            '<div style="display:flex;flex-direction:column;align-items:center;justify-content:center;' +
            'padding:60px 20px;text-align:center;gap:12px">' +
            '<svg width="36" height="36" style="color:var(--danger);opacity:.7">' +
            '<use href="#icon-warning"/>' +
            '</svg>' +
            '<div style="font-size:14px;font-weight:600;color:var(--text-2)">Failed to load</div>' +
            '<div style="font-size:13px;color:var(--text-3);max-width:280px">' +
            message +
            '</div>' +
            (onRetry
                ? '<button class="btn btn-secondary" style="margin-top:8px" onclick="(' +
                onRetry.toString() +
                ')()">' +
                '<svg width="13" height="13"><use href="#icon-refresh"/></svg>' +
                'Try again</button>'
                : '') +
            '</div>';

        container.innerHTML = html;
    };

    /**
     * showContent(container, html)
     * Simply sets the container innerHTML — the "normal" state.
     */
    window.showContent = function (container, html) {
        container.innerHTML = html;
    };

    /* ── Skeleton animation keyframe ── injected once */
    if (!document.getElementById('skeleton-style')) {
        const s = document.createElement('style');
        s.id = 'skeleton-style';
        s.textContent = `@keyframes skeleton-pulse {
            0%   { opacity: 1; }
            50%  { opacity: 0.4; }
            100% { opacity: 1; }
        }`;
        document.head.appendChild(s);
    }

})();


