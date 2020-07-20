package com.example.stockwatch;

public class Stock implements Comparable<Stock>{
    private String symbol;
    private String companyName;
    private double price;
    private double priceChange;
    private double changePercentage;

    public Stock(String symbol, String companyName, double price, double priceChange, double changePercentage) {
        this.symbol = symbol;
        this.companyName = companyName;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercentage = changePercentage;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public double getPrice() {
        return price;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "symbol='" + symbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", price=" + price +
                ", priceChange=" + priceChange +
                ", changePercentage=" + changePercentage +
                '}';
    }

    @Override
    public int compareTo(Stock s) {
        return this.getSymbol().compareTo(s.getSymbol());
    }
}
