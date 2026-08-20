/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: '#0F1115', // Deep dark
        surface: '#1C1F26',    // Dark grey surface
        surfaceElevated: '#2A2F3A',
        primary: '#EAB308',    // Premium Gold (yellow-500)
        primaryHover: '#CA8A04',
        textPrimary: '#FFFFFF',
        textSecondary: '#9CA3AF', // Gray 400
        border: '#374151',        // Gray 700
        success: '#10B981',       // Emerald 500
        error: '#EF4444',         // Red 500
        warning: '#F59E0B',       // Amber 500
        
        // Semantic Seat Colors
        seat: {
          available: '#4B5563', // Gray 600
          selected: '#EAB308',  // Primary Gold
          sold: '#374151',      // Gray 700
          reserved: '#F59E0B',  // Amber
          vip: '#8B5CF6',       // Violet 500
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Inter', 'system-ui', 'sans-serif'],
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-in-out',
        'slide-up': 'slideUp 0.4s ease-out',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(20px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        }
      }
    },
  },
  plugins: [],
}
