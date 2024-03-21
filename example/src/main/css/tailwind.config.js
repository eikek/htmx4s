// tailwind.config.js

const colors = require('tailwindcss/colors')

module.exports = {
    darkMode: 'class', // or 'media' or 'class'
    content: [ "example/src/main/scala/htmx4s/example/contacts/Views.scala",
               "example/src/main/css/keep.txt",
             ],
    theme: {
        extend: {
            screens: {
                '3xl': '1792px',
                '4xl': '2048px',
                '5xl': '2560px',
                '6xl': '3072px',
                '7xl': '3584px'
            }
        }
    },
    plugins: [
    ]
}
