import * as XLSX from "xlsx";

/**
 * 将 JSON 数据导出为 Excel
 * @param {Array} data - 需要导出的原始数组
 * @param {String} fileName - 导出的文件名
 * @param {Object} headerMap - 字段映射映射（把英文 Key 换成中文标题）
 */
export const exportToExcel = (data, fileName, headerMap) => {
  // 1. 数据格式化：将原始的 JSON 数据映射为中文标题的数据
  const formattedData = data.map((item) => {
    const newItem = {};
    for (const key in headerMap) {
      newItem[headerMap[key]] = item[key];
    }
    return newItem;
  });

  // 2. 创建工作表 (Worksheet)
  const worksheet = XLSX.utils.json_to_sheet(formattedData);

  // 3. 创建工作簿 (Workbook) 并添加工作表
  const workbook = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(workbook, worksheet, "Sheet1");

  // 4. 生成并下载文件
  // 增加时间戳，防止同名文件覆盖，这是一个专业的小细节
  const timestamp = new Date().toISOString().split("T")[0];
  XLSX.writeFile(workbook, `${fileName}_${timestamp}.xlsx`);
};
