/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        display: ['Outfit', 'sans-serif'],
        body: ['Plus Jakarta Sans', 'sans-serif'],
      },
      colors: {
        dark: {
          bg: '#0A0A0F',
          card: 'rgba(18, 18, 26, 0.8)',
          border: 'rgba(255, 255, 255, 0.08)',
        },
        light: {
          bg: '#FAFAFA',
          card: '#FFFFFF',
          border: 'rgba(0, 0, 0, 0.06)',
        },
        brand: {
          orange: '#FF8C42',
          coral: '#F85E00',
          amber: '#FF6B35',
        }
      },
      backgroundImage: {
        'gradient-brand': 'linear-gradient(135deg, #FF8C42 0%, #F85E00 50%, #FF6B35 100%)',
      },
      animation: {
        'fade-in-up': 'fadeInUp 0.6s ease-out forwards',
        'float': 'float 3s ease-in-out infinite',
      },
      keyframes: {
        fadeInUp: {
          '0%': { opacity: '0', transform: 'translateY(20px)' },
          '100%': { opacity: '1', transform: 'translateY(0)' },
        },
        float: {
          '0%, 100%': { transform: 'translateY(0)' },
          '50%': { transform: 'translateY(-10px)' },
        },
      },
    },
  },
  plugins: [],
}
