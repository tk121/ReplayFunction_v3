# ===============================
# 設定
# ===============================

# コピー元フォルダ
$source = "C:\pleiades\2025-12\workspace\ReplayFunction_v3\src\main"

# コピー先フォルダ
$dest = "C:\Users\t-npc\Desktop\Copy-FilesByExtension"

# 対象拡張子（複数指定）
$extensions = @(".java",".js", ".html")

# ===============================
# 出力フォルダ作成
# ===============================

# sourceの最下位フォルダ名取得
$sourceFolderName = Split-Path $source -Leaf

# 日付
$timestamp = Get-Date -Format "yyyyMMddHHmmss"

# 出力フォルダ名
$destFolder = Join-Path $dest ($sourceFolderName + "_" + $timestamp)

# フォルダ作成
New-Item -ItemType Directory -Path $destFolder -Force | Out-Null

# ===============================
# ファイルコピー
# ===============================

Get-ChildItem -Path $source -Recurse -File | Where-Object {
    $extensions -contains $_.Extension
} | ForEach-Object {

    Copy-Item $_.FullName -Destination $destFolder
}

Write-Host "コピー完了:"
Write-Host $destFolder