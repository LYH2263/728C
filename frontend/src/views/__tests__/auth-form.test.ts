import fs from 'node:fs'
import path from 'node:path'
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import RichText from '@/components/RichText.vue'

describe('auth forms', () => {
  it('login password input should stay masked without reveal toggle', () => {
    const source = fs.readFileSync(path.resolve(__dirname, '../Login.vue'), 'utf-8')
    expect(source).toContain('type="password"')
    expect(source).not.toContain('show-password')
  })

  it('register password inputs should stay masked without reveal toggle', () => {
    const source = fs.readFileSync(path.resolve(__dirname, '../Register.vue'), 'utf-8')
    expect(source).not.toContain('show-password')
    expect((source.match(/type="password"/g) || []).length).toBeGreaterThanOrEqual(2)
  })
})

describe('rich text sanitization', () => {
  it('removes dangerous script tags before rendering html', () => {
    const wrapper = mount(RichText, {
      props: {
        content: '<p>safe</p><script>alert(1)</script><img src="/demo.png" />',
      },
    })

    expect(wrapper.html()).toContain('<p>safe</p>')
    expect(wrapper.html()).toContain('<img src="/demo.png">')
    expect(wrapper.html()).not.toContain('<script')
    expect(wrapper.html()).not.toContain('alert(1)')
  })
})
