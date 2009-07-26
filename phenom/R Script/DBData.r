drv <- dbDriver("SQLite")
con <- dbConnect(drv, dbname = "E:/fei/SQLite/SuperT_STOCK.sqlite")
res <- dbSendQuery (con, "select * from STOCK_PRICE where Symbol = '000001' and Date between '20080101' and '20081231' and Exchange = 'sh'")
df <- fetch(res, -1)

setwd("C:/")
df.exchanges <- unique(df$Exchange)

for(i in 1:length(df.exchanges)) {
    Nest.i <- df.exchanges[i]
    df.i <- df[df$Exchange == Nest.i,]
    fileName <- paste(Nest.i, ".jpg", sep="")
    jpeg(file = fileName)
    plot(x = df$Date, y = df$Open, xlab = 'Date', ylab = 'Open Price', main = Nest.i)
    dev.off()
}

ZerosPerVariable <- function(X1) {
  D1 = (X1 == 0)
  colSums(D1)
}


NAPerVariable <- function(X1) {
  D1 <- is.na(X1)
  colSums(D1)
}

names(df)
str(df)
dbClearResult(res)
dbDisconnect(drv)
