// tailwind.config.js

const colors = require('tailwindcss/colors')

export default {
    content: [ "src/**/*.scala",
               "webapp/css/keep.txt",
             ],
    darkMode: 'media',
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
