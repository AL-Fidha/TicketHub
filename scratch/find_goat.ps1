$url = "http://localhost:8081/api/movies"
$movies = Invoke-RestMethod -Uri $url
$goat = $movies | Where-Object { $_.title -like "*Goat*" -or $_.title -like "*Greatest*" }
Write-Output "Matching movies count: $($goat.Count)"
foreach ($g in $goat) {
    Write-Output "Found: ID $($g.id) - $($g.title)"
}
