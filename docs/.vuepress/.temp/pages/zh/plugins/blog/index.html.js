import comp from "/Users/yc/github/YCBlogBook/docs/.vuepress/.temp/pages/zh/plugins/blog/index.html.vue"
const data = JSON.parse("{\"path\":\"/zh/plugins/blog/\",\"title\":\"博客插件\",\"lang\":\"zh-CN\",\"frontmatter\":{\"description\":\"博客插件\",\"head\":[[\"link\",{\"rel\":\"alternate\",\"hreflang\":\"en-us\",\"href\":\"https://ecosystem.vuejs.press/plugins/blog/\"}],[\"meta\",{\"property\":\"og:url\",\"content\":\"https://ecosystem.vuejs.press/zh/plugins/blog/\"}],[\"meta\",{\"property\":\"og:site_name\",\"content\":\"打工充学习网站\"}],[\"meta\",{\"property\":\"og:title\",\"content\":\"博客插件\"}],[\"meta\",{\"property\":\"og:description\",\"content\":\"博客插件\"}],[\"meta\",{\"property\":\"og:type\",\"content\":\"article\"}],[\"meta\",{\"property\":\"og:locale\",\"content\":\"zh-CN\"}],[\"meta\",{\"property\":\"og:locale:alternate\",\"content\":\"en-US\"}],[\"script\",{\"type\":\"application/ld+json\"},\"{\\\"@context\\\":\\\"https://schema.org\\\",\\\"@type\\\":\\\"Article\\\",\\\"headline\\\":\\\"博客插件\\\",\\\"image\\\":[\\\"\\\"],\\\"dateModified\\\":null,\\\"author\\\":[]}\"]]},\"headers\":[],\"autoDesc\":true,\"filePathRelative\":\"zh/plugins/blog/README.md\"}")
export { comp, data }

if (import.meta.webpackHot) {
  import.meta.webpackHot.accept()
  if (__VUE_HMR_RUNTIME__.updatePageData) {
    __VUE_HMR_RUNTIME__.updatePageData(data)
  }
}

if (import.meta.hot) {
  import.meta.hot.accept(({ data }) => {
    __VUE_HMR_RUNTIME__.updatePageData(data)
  })
}
