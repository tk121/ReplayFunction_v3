let rawTrendData = {
  vdu1: {},
  vdu2: {},
  vdu3: {},
  vdu4: {},
  alert1: {},
  alert2: {},
  alertElectrical: {}
};

let currentDisplayRangeHours = 4;

const SERIES_KEYS = ["vdu1", "vdu2", "vdu3", "vdu4", "alert1", "alert2", "alertElectrical"];
const CHART_WIDTH = 900;
const CHART_HEIGHT = 260;
const MARGIN = { top: 20, right: 20, bottom: 40, left: 60 };

document.addEventListener("DOMContentLoaded", () => {
  bindRangeEvents();
});

function bindRangeEvents() {
  const radios = document.querySelectorAll('input[name="range"]');
  radios.forEach(radio => {
    radio.addEventListener("change", (event) => {
      currentDisplayRangeHours = Number(event.target.value);
      redrawAllCharts();
    });
  });
}

function onTrendDataReceived(json) {
  rawTrendData = normalizeRawTrendData(json || {});
  redrawAllCharts();
}
window.onTrendDataReceived = onTrendDataReceived;

function normalizeRawTrendData(json) {
  const normalized = {};
  for (const key of SERIES_KEYS) {
    normalized[key] = json[key] || {};
  }
  return normalized;
}

function getAggregationMinutes(displayRangeHours) {
  if (displayRangeHours === 4) return 1;
  if (displayRangeHours === 12) return 3;
  if (displayRangeHours === 24) return 6;
  return 1;
}

function parseYmdHms(text) {
  const yyyy = Number(text.substring(0, 4));
  const mm = Number(text.substring(4, 6)) - 1;
  const dd = Number(text.substring(6, 8));
  const hh = Number(text.substring(9, 11));
  const mi = Number(text.substring(12, 14));
  const ss = Number(text.substring(15, 17));
  return new Date(yyyy, mm, dd, hh, mi, ss);
}

function formatYmdHms(date) {
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  const hh = String(date.getHours()).padStart(2, "0");
  const mi = String(date.getMinutes()).padStart(2, "0");
  const ss = String(date.getSeconds()).padStart(2, "0");
  return `${yyyy}${mm}${dd} ${hh}:${mi}:${ss}`;
}

function toTimeValueArray(seriesData) {
  return Object.entries(seriesData)
    .map(([time, value]) => ({
      time,
      date: parseYmdHms(time),
      value: value == null ? 0 : Number(value)
    }))
    .sort((a, b) => a.date - b.date);
}

function floorToBucket(date, unitMinutes) {
  const d = new Date(date.getTime());
  d.setSeconds(0, 0);
  const minute = d.getMinutes();
  d.setMinutes(Math.floor(minute / unitMinutes) * unitMinutes);
  return d;
}

function aggregateSeries(series, unitMinutes) {
  if (unitMinutes === 1) {
    return series.map(item => ({
      time: item.time,
      date: item.date,
      value: item.value
    }));
  }

  const bucketMap = new Map();
  for (const item of series) {
    const bucketDate = floorToBucket(item.date, unitMinutes);
    const bucketKey = formatYmdHms(bucketDate);
    if (!bucketMap.has(bucketKey)) {
      bucketMap.set(bucketKey, {
        time: bucketKey,
        date: bucketDate,
        value: 0
      });
    }
    bucketMap.get(bucketKey).value += item.value;
  }

  return Array.from(bucketMap.values()).sort((a, b) => a.date - b.date);
}

function filterSeriesByDisplayRange(series, displayRangeHours) {
  if (!series || series.length === 0) {
    return [];
  }
  const lastDate = series[series.length - 1].date;
  const fromDate = new Date(lastDate.getTime() - displayRangeHours * 60 * 60 * 1000);
  return series.filter(item => item.date >= fromDate);
}

function buildDisplaySeries(seriesKey) {
  const source = rawTrendData[seriesKey] || {};
  const minuteSeries = toTimeValueArray(source);
  const aggregated = aggregateSeries(minuteSeries, getAggregationMinutes(currentDisplayRangeHours));
  return filterSeriesByDisplayRange(aggregated, currentDisplayRangeHours);
}

function redrawAllCharts() {
  drawLineChart("#chart-vdu1", buildDisplaySeries("vdu1"), "VDU1");
  drawLineChart("#chart-vdu2", buildDisplaySeries("vdu2"), "VDU2");
  drawLineChart("#chart-vdu3", buildDisplaySeries("vdu3"), "VDU3");
  drawLineChart("#chart-vdu4", buildDisplaySeries("vdu4"), "VDU4");
  drawLineChart("#chart-alert1", buildDisplaySeries("alert1"), "ALERT1");
  drawLineChart("#chart-alert2", buildDisplaySeries("alert2"), "ALERT2");
  drawLineChart("#chart-alertElectrical", buildDisplaySeries("alertElectrical"), "ALERT ELECTRICAL");
}
window.redrawAllCharts = redrawAllCharts;

function drawLineChart(containerSelector, series, title) {
  const container = d3.select(containerSelector);
  if (container.empty()) {
    return;
  }

  container.selectAll("*").remove();

  if (!series || series.length === 0) {
    container.append("div").text(`${title}: 表示データがありません`);
    return;
  }

  const width = CHART_WIDTH - MARGIN.left - MARGIN.right;
  const height = CHART_HEIGHT - MARGIN.top - MARGIN.bottom;

  const svg = container.append("svg")
    .attr("width", CHART_WIDTH)
    .attr("height", CHART_HEIGHT);

  const g = svg.append("g")
    .attr("transform", `translate(${MARGIN.left},${MARGIN.top})`);

  const x = d3.scaleTime()
    .domain(d3.extent(series, d => d.date))
    .range([0, width]);

  const maxValue = d3.max(series, d => d.value) || 0;
  const y = d3.scaleLinear()
    .domain([0, maxValue === 0 ? 1 : maxValue])
    .nice()
    .range([height, 0]);

  g.append("g")
    .attr("transform", `translate(0,${height})`)
    .call(d3.axisBottom(x).ticks(6).tickFormat(d3.timeFormat("%H:%M")));

  g.append("g")
    .call(d3.axisLeft(y).ticks(5).tickFormat(d3.format("d")));

  g.append("g")
    .call(d3.axisLeft(y).ticks(5).tickSize(-width).tickFormat(""));

  const line = d3.line()
    .x(d => x(d.date))
    .y(d => y(d.value));

  g.append("path")
    .datum(series)
    .attr("fill", "none")
    .attr("stroke", "#1f77b4")
    .attr("stroke-width", 1.5)
    .attr("d", line);

  g.selectAll(".point")
    .data(series)
    .enter()
    .append("circle")
    .attr("class", "point")
    .attr("cx", d => x(d.date))
    .attr("cy", d => y(d.value))
    .attr("r", 3)
    .attr("fill", "#1f77b4");

  svg.append("text")
    .attr("x", MARGIN.left)
    .attr("y", 14)
    .text(`${title} (${currentDisplayRangeHours}時間表示 / ${getAggregationMinutes(currentDisplayRangeHours)}分集計)`);
}