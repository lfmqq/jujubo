/**
 * 通用导出工具模块
 * 支持：Excel (.xlsx)、PDF (.pdf)、Word (.docx)、ZIP (.zip，含以上三种格式)
 *
 * 使用方式：
 *   import { exportExcel, exportPDF, exportWord, exportZip } from '@/utils/export'
 *
 *   const columns = [
 *     { prop: 'username', label: '用户名' },
 *     { prop: 'nickname', label: '昵称' },
 *   ]
 *   exportExcel({ columns, data: userList, fileName: '用户列表' })
 */

import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import { Document, Packer, Paragraph, Table, TableRow, TableCell, TextRun, WidthType, AlignmentType, HeadingLevel } from 'docx'
import JSZip from 'jszip'
import { saveAs } from 'file-saver'

/**
 * 从数据生成表头 + 行数组，供各导出格式复用
 * @param {Array<{prop: string, label: string}>} columns
 * @param {Array<Object>} data
 * @returns {{ headers: string[], rows: string[][] }}
 */
function pickData(columns, data) {
  const headers = columns.map(c => c.label)
  const rows = data.map(row =>
    columns.map(c => (row[c.prop] != null ? String(row[c.prop]) : ''))
  )
  return { headers, rows }
}

/**
 * 获取今天的日期字符串 YYYY-MM-DD
 */
function today() {
  const d = new Date()
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

/**
 * 生成隐藏 HTML 表格并用 html2canvas 渲染为图片
 * 解决 jsPDF 默认不支持中文的问题
 */
async function buildTableImage({ title, count, headers, rows, width = 1280 }) {
  const container = document.createElement('div')
  container.style.position = 'fixed'
  container.style.top = '-9999px'
  container.style.left = '-9999px'
  container.style.width = `${width}px`
  container.style.padding = '24px'
  container.style.background = '#fff'
  container.style.fontFamily = '"Microsoft YaHei", "PingFang SC", sans-serif'
  container.style.fontSize = '14px'
  container.style.color = '#333'

  const thead = headers.map(h => `<th style="padding:10px 12px;border:1px solid #dcdfe6;background:#409eff;color:#fff;font-weight:600;text-align:center;white-space:nowrap;">${h}</th>`).join('')
  const tbody = rows.map((row, idx) =>
    `<tr style="background:${idx % 2 === 1 ? '#f5f7fa' : '#fff'};">` +
    row.map(v => `<td style="padding:8px 12px;border:1px solid #dcdfe6;text-align:center;white-space:nowrap;">${v}</td>`).join('') +
    '</tr>'
  ).join('')

  container.innerHTML = `
    <div style="margin-bottom:16px;">
      <div style="font-size:20px;font-weight:600;margin-bottom:8px;">${title}</div>
      <div style="font-size:12px;color:#666;">导出时间：${today()}&nbsp;&nbsp;&nbsp;&nbsp;共 ${count} 条记录</div>
    </div>
    <table style="width:100%;border-collapse:collapse;border:1px solid #dcdfe6;">
      <thead><tr>${thead}</tr></thead>
      <tbody>${tbody}</tbody>
    </table>
  `

  document.body.appendChild(container)
  try {
    const canvas = await html2canvas(container, {
      scale: 2,
      useCORS: true,
      backgroundColor: '#ffffff'
    })
    return { imgData: canvas.toDataURL('image/png'), imgHeight: canvas.height, imgWidth: canvas.width }
  } finally {
    document.body.removeChild(container)
  }
}

/**
 * 导出为 Excel (.xlsx)
 * @param {Object} options
 * @param {Array<{prop: string, label: string}>} options.columns 列定义
 * @param {Array<Object>} options.data 数据
 * @param {string} [options.fileName] 文件名（不含扩展名）
 */
export function exportExcel({ columns, data, fileName = 'export' }) {
  const { headers, rows } = pickData(columns, data)
  const sheet = XLSX.utils.aoa_to_sheet([headers, ...rows])

  // 设置列宽
  sheet['!cols'] = columns.map(() => ({ wch: 20 }))

  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, sheet, 'Sheet1')
  const buffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' })
  saveAs(new Blob([buffer], { type: 'application/octet-stream' }), `${fileName}.xlsx`)
}

/**
 * 导出为 PDF (.pdf)
 * @param {Object} options
 * @param {Array<{prop: string, label: string}>} options.columns 列定义
 * @param {Array<Object>} options.data 数据
 * @param {string} [options.fileName] 文件名（不含扩展名）
 * @param {string} [options.title] 标题，显示在 PDF 顶部
 */
export async function exportPDF({ columns, data, fileName = 'export', title = '数据报表' }) {
  const { headers, rows } = pickData(columns, data)
  const { imgData, imgWidth, imgHeight } = await buildTableImage({
    title,
    count: data.length,
    headers,
    rows
  })

  const doc = new jsPDF('l', 'mm', 'a4')
  const pageWidth = doc.internal.pageSize.getWidth()
  const pageHeight = doc.internal.pageSize.getHeight()
  const margin = 10
  const availableWidth = pageWidth - margin * 2
  const availableHeight = pageHeight - margin * 2

  const ratio = Math.min(
    availableWidth / imgWidth,
    availableHeight / imgHeight
  )

  let currentY = margin
  let currentHeight = imgHeight * ratio

  // 如果单页放不下，按宽度缩放并分页
  const renderWidth = availableWidth
  const renderHeight = (imgHeight / imgWidth) * renderWidth
  let heightLeft = renderHeight
  let position = currentY

  doc.addImage(imgData, 'PNG', margin, position, renderWidth, renderHeight)
  heightLeft -= availableHeight

  while (heightLeft > 0) {
    position = heightLeft - renderHeight + margin
    doc.addPage()
    doc.addImage(imgData, 'PNG', margin, position, renderWidth, renderHeight)
    heightLeft -= availableHeight
  }

  doc.save(`${fileName}.pdf`)
}

/**
 * 导出为 Word (.docx)
 * @param {Object} options
 * @param {Array<{prop: string, label: string}>} options.columns 列定义
 * @param {Array<Object>} options.data 数据
 * @param {string} [options.fileName] 文件名（不含扩展名）
 * @param {string} [options.title] 标题
 */
export async function exportWord({ columns, data, fileName = 'export', title = '数据报表' }) {
  const { headers, rows } = pickData(columns, data)

  // 将数值型字段自动右对齐，其他左对齐
  const alignCell = (val) => {
    if (val !== '' && !isNaN(val)) return AlignmentType.RIGHT
    return AlignmentType.LEFT
  }

  const doc = new Document({
    sections: [{
      children: [
        new Paragraph({
          text: title,
          heading: HeadingLevel.HEADING_1,
          alignment: AlignmentType.CENTER,
          spacing: { after: 200 }
        }),
        new Paragraph({
          text: `导出时间：${today()}    共 ${data.length} 条记录`,
          spacing: { after: 300 },
          style: 'normal'
        }),
        new Table({
          width: { size: 100, type: WidthType.PERCENTAGE },
          rows: [
            // 表头行
            new TableRow({
              tableHeader: true,
              children: headers.map(h =>
                new TableCell({
                  children: [new Paragraph({
                    children: [new TextRun({ text: h, bold: true, size: 20, font: 'Microsoft YaHei' })],
                    alignment: AlignmentType.CENTER
                  })],
                  shading: { fill: '409EFF' },
                  width: { size: 100 / headers.length, type: WidthType.PERCENTAGE }
                })
              )
            }),
            // 数据行
            ...rows.map(row =>
              new TableRow({
                children: row.map((val, idx) =>
                  new TableCell({
                    children: [new Paragraph({
                      children: [new TextRun({ text: val, size: 20, font: 'Microsoft YaHei' })],
                      alignment: alignCell(row[idx])
                    })],
                    width: { size: 100 / headers.length, type: WidthType.PERCENTAGE }
                  })
                )
              })
            )
          ]
        })
      ]
    }]
  })

  const buffer = await Packer.toBlob(doc)
  saveAs(buffer, `${fileName}.docx`)
}

/**
 * 导出为 ZIP 压缩包（包含 Excel + PDF + Word）
 * @param {Object} options
 * @param {Array<{prop: string, label: string}>} options.columns 列定义
 * @param {Array<Object>} options.data 数据
 * @param {string} [options.fileName] 文件名（不含扩展名）
 * @param {string} [options.title] 标题
 */
export async function exportZip({ columns, data, fileName = 'export', title = '数据报表' }) {
  const zip = new JSZip()

  // Excel
  const { headers, rows } = pickData(columns, data)
  const sheet = XLSX.utils.aoa_to_sheet([headers, ...rows])
  sheet['!cols'] = columns.map(() => ({ wch: 20 }))
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, sheet, 'Sheet1')
  zip.file(`${fileName}.xlsx`, XLSX.write(wb, { bookType: 'xlsx', type: 'array' }))

  // PDF
  const { imgData, imgWidth, imgHeight } = await buildTableImage({
    title,
    count: data.length,
    headers,
    rows
  })
  const doc = new jsPDF('l', 'mm', 'a4')
  const pageWidth = doc.internal.pageSize.getWidth()
  const pageHeight = doc.internal.pageSize.getHeight()
  const margin = 10
  const availableWidth = pageWidth - margin * 2
  const availableHeight = pageHeight - margin * 2
  const renderWidth = availableWidth
  const renderHeight = (imgHeight / imgWidth) * renderWidth
  let heightLeft = renderHeight
  let position = margin
  doc.addImage(imgData, 'PNG', margin, position, renderWidth, renderHeight)
  heightLeft -= availableHeight
  while (heightLeft > 0) {
    position = heightLeft - renderHeight + margin
    doc.addPage()
    doc.addImage(imgData, 'PNG', margin, position, renderWidth, renderHeight)
    heightLeft -= availableHeight
  }
  zip.file(`${fileName}.pdf`, doc.output('arraybuffer'))

  // Word
  const alignCell = (val) => {
    if (val !== '' && !isNaN(val)) return AlignmentType.RIGHT
    return AlignmentType.LEFT
  }
  const wordDoc = new Document({
    sections: [{
      children: [
        new Paragraph({
          text: title,
          heading: HeadingLevel.HEADING_1,
          alignment: AlignmentType.CENTER,
          spacing: { after: 200 }
        }),
        new Paragraph({
          text: `导出时间：${today()}    共 ${data.length} 条记录`,
          spacing: { after: 300 },
          style: 'normal'
        }),
        new Table({
          width: { size: 100, type: WidthType.PERCENTAGE },
          rows: [
            new TableRow({
              tableHeader: true,
              children: headers.map(h =>
                new TableCell({
                  children: [new Paragraph({
                    children: [new TextRun({ text: h, bold: true, size: 20, font: 'Microsoft YaHei' })],
                    alignment: AlignmentType.CENTER
                  })],
                  shading: { fill: '409EFF' }
                })
              )
            }),
            ...rows.map(row =>
              new TableRow({
                children: row.map(val =>
                  new TableCell({
                    children: [new Paragraph({
                      children: [new TextRun({ text: val, size: 20, font: 'Microsoft YaHei' })],
                      alignment: alignCell(val)
                    })]
                  })
                )
              })
            )
          ]
        })
      ]
    }]
  })
  const wordBuffer = await Packer.toBlob(wordDoc)
  zip.file(`${fileName}.docx`, wordBuffer)

  const zipBlob = await zip.generateAsync({ type: 'blob' })
  saveAs(zipBlob, `${fileName}.zip`)
}
