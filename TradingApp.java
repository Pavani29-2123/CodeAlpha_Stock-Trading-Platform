import java.io.*;
import java.util.*;

class Stock {
    String symbol;
    String name;
    double price;

    public Stock(String symbol, String name, double price) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public void updatePrice() {
       
        double change = (Math.random() * 10 - 5); 
        price += price * change / 100;
        price = Math.round(price * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return symbol + " - " + name + " | $" + price;
    }
}

class Transaction {
    String type; 
    String stockSymbol;
    int quantity;
    double price;
    Date date;

    public Transaction(String type, String stockSymbol, int quantity, double price) {
        this.type = type;
        this.stockSymbol = stockSymbol;
        this.quantity = quantity;
        this.price = price;
        this.date = new Date();
    }

    @Override
    public String toString() {
        return date + " | " + type + " | " + stockSymbol + " | Qty: " + quantity + " | Price: $" + price;
    }
}

class User {
    String username;
    double cash;
    Map<String, Integer> portfolio;
    List<Transaction> transactions;

    public User(String username, double startingCash) {
        this.username = username;
        this.cash = startingCash;
        this.portfolio = new HashMap<>();
        this.transactions = new ArrayList<>();
    }

    public void buyStock(Stock stock, int quantity) {
        double cost = stock.price * quantity;
        if (cash >= cost) {
            cash -= cost;
            portfolio.put(stock.symbol, portfolio.getOrDefault(stock.symbol, 0) + quantity);
            transactions.add(new Transaction("BUY", stock.symbol, quantity, stock.price));
            System.out.println("Purchased " + quantity + " shares of " + stock.symbol);
        } else {
            System.out.println("Insufficient funds.");
        }
    }

    public void sellStock(Stock stock, int quantity) {
        int owned = portfolio.getOrDefault(stock.symbol, 0);
        if (owned >= quantity) {
            double proceeds = stock.price * quantity;
            cash += proceeds;
            portfolio.put(stock.symbol, owned - quantity);
            transactions.add(new Transaction("SELL", stock.symbol, quantity, stock.price));
            System.out.println("Sold " + quantity + " shares of " + stock.symbol);
        } else {
            System.out.println("You don't own enough shares.");
        }
    }

    public void viewPortfolio(Map<String, Stock> marketStocks) {
        System.out.println("\n--- Portfolio ---");
        if (portfolio.isEmpty()) {
            System.out.println("No holdings.");
        } else {
            double totalValue = cash;
            for (String symbol : portfolio.keySet()) {
                int qty = portfolio.get(symbol);
                double price = marketStocks.get(symbol).price;
                double value = qty * price;
                totalValue += value;
                System.out.printf("%s | Qty: %d | Price: $%.2f | Value: $%.2f%n",
                        symbol, qty, price, value);
            }
            System.out.printf("Cash: $%.2f%n", cash);
            System.out.printf("Total Portfolio Value: $%.2f%n", totalValue);
        }
    }

    public void viewTransactions() {
        System.out.println("\n--- Transaction History ---");
        if (transactions.isEmpty()) {
            System.out.println("No transactions.");
        } else {
            for (Transaction t : transactions) {
                System.out.println(t);
            }
        }
    }
}

class Market {
    Map<String, Stock> stocks;

    public Market() {
        stocks = new HashMap<>();
        
        stocks.put("AAPL", new Stock("AAPL", "Apple Inc.", 170.00));
        stocks.put("GOOG", new Stock("GOOG", "Alphabet Inc.", 2800.00));
        stocks.put("TSLA", new Stock("TSLA", "Tesla Inc.", 720.00));
        stocks.put("AMZN", new Stock("AMZN", "Amazon.com Inc.", 3400.00));
    }

    public void updatePrices() {
        for (Stock stock : stocks.values()) {
            stock.updatePrice();
        }
    }

    public void displayMarket() {
        System.out.println("\n--- Market Data ---");
        for (Stock stock : stocks.values()) {
            System.out.println(stock);
        }
    }

    public Stock getStock(String symbol) {
        return stocks.get(symbol.toUpperCase());
    }
}

public class TradingApp {
    static Scanner scanner = new Scanner(System.in);
    static User user;
    static Market market = new Market();

    public static void main(String[] args) {
        loadUser();
        int choice;

        do {
            System.out.println("\n=== Stock Trading Simulator ===");
            System.out.println("1. View Market");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. View Portfolio");
            System.out.println("5. View Transactions");
            System.out.println("6. Save & Exit");
            System.out.print("Choose an option: ");
            choice = scanner.nextInt();
            scanner.nextLine(); 

            market.updatePrices();

            switch (choice) {
                case 1:
                    market.displayMarket();
                    break;
                case 2:
                    buyFlow();
                    break;
                case 3:
                    sellFlow();
                    break;
                case 4:
                    user.viewPortfolio(market.stocks);
                    break;
                case 5:
                    user.viewTransactions();
                    break;
                case 6:
                    saveUser();
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 6);
    }

    public static void buyFlow() {
        market.displayMarket();
        System.out.print("Enter stock symbol to buy: ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stock = market.getStock(symbol);
        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }

        System.out.print("Enter quantity to buy: ");
        int qty = scanner.nextInt();
        scanner.nextLine(); 
        user.buyStock(stock, qty);
    }

    public static void sellFlow() {
        user.viewPortfolio(market.stocks);
        System.out.print("Enter stock symbol to sell: ");
        String symbol = scanner.nextLine().toUpperCase();
        Stock stock = market.getStock(symbol);
        if (stock == null) {
            System.out.println("Stock not found.");
            return;
        }

        System.out.print("Enter quantity to sell: ");
        int qty = scanner.nextInt();
        scanner.nextLine(); 
        user.sellStock(stock, qty);
    }

    public static void loadUser() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("user.dat"))) {
            user = (User) in.readObject();
            System.out.println("Welcome back, " + user.username);
        } catch (Exception e) {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            user = new User(username, 10000.00);
            System.out.println("New user created.");
        }
    }

    public static void saveUser() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("user.dat"))) {
            out.writeObject(user);
            System.out.println("User data saved.");
        } catch (IOException e) {
            System.out.println("Error saving user data.");
        }
    }
}
