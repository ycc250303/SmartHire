import {
  defineConfig,
  presetAttributify,
  presetIcons,
  presetUno,
  presetWind
} from 'unocss'

export default defineConfig({
  presets: [
    presetUno(),
    presetAttributify(),
    presetWind(),
    presetIcons({
      scale: 1.2,
      warn: true
    })
  ],
  theme: {
    colors: {
      primary: {
        DEFAULT: '#2f7cff',
        hover: '#1e5fcc',
        light: '#e5edff'
      },
      success: {
        DEFAULT: '#52c41a',
        hover: '#389e0d',
        light: '#f6ffed'
      },
      warning: {
        DEFAULT: '#faad14',
        hover: '#d48806',
        light: '#fffbe6'
      },
      error: {
        DEFAULT: '#ff5f5f',
        hover: '#ff3838',
        light: '#fff1f0'
      },
      info: {
        DEFAULT: '#2080f0',
        hover: '#1a73e8',
        light: '#f0f9ff'
      }
    },
    fontSize: {
      'icon-xs': '0.875rem',
      'icon-small': '1rem',
      'icon': '1.125rem',
      'icon-large': '1.5rem',
      'icon-xl': '2rem'
    }
  },
  shortcuts: {
    'flex-center': 'flex items-center justify-center',
    'flex-between': 'flex items-center justify-between',
    'flex-col-center': 'flex flex-col items-center justify-center',
    'card-shadow': 'shadow-[0_12rpx_32rpx_rgba(31,55,118,0.08)]',
    'card-wrapper': 'rounded-6px bg-white card-shadow'
  },
  rules: [
    [/^text-(\d+)$/, ([, d]) => ({ 'font-size': `${d / 75}rem` })],
    [/^p-(\d+)$/, ([, d]) => ({ 'padding': `${d / 75}rem` })],
    [/^m-(\d+)$/, ([, d]) => ({ 'margin': `${d / 75}rem` })]
  ]
})