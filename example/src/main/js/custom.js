// Flag htmx requests to get htmx, and flag it being sent from htmx
// https://github.com/bigskysoftware/htmx/issues/1229
document.addEventListener('htmx:configRequest', evt => {
    evt.detail.headers['Accept'] = 'text/html;hx=1'
});

const C = {
    addClass: function(event, sel, cls) {
        if (event && event.target) {
            event.target.querySelectorAll(sel).forEach(item =>
                item.classList.add(cls)
            );
        }
    },

    removeClass: function(event, sel, cls) {
        if (event && event.target) {
            event.target.querySelectorAll(sel).forEach(item =>
                item.classList.remove(cls)
            );
        }
    },

    toggleClass: function(event, sel, cls) {
        if (event && event.target) {
            event.target.querySelectorAll(sel).forEach(item =>
                item.classList.toggle(cls)
            );
        }
    },

    moveClass: function(event, sel, from, to) {
        if (event && event.target) {
            event.target.querySelectorAll(sel).forEach(item => {
                item.classList.remove(from);
                item.classList.add(to);
            });
        }
    }
};
