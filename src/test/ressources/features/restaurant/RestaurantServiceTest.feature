Feature: Restaurant Service Feature

  Scenario: Viewing a restaurant's menu
    Given a restaurant service
    When I view the menu for restaurant with ID 1
    Then I should see 3 menu items
  Scenario: Adding a menu item to a restaurant

    Given a restaurant service
    When I add a menu item "Pasta Carbonara" with price 15.0 to restaurant with ID 1
    Then the menu for restaurant with ID 1 should include "Pasta Carbonara" menu item with price 15.0

  Scenario: Removing a menu item from a restaurant
    Given a restaurant service
    When I remove the menu item "Steak au poivre" from restaurant with ID 1
    Then the menu for restaurant with ID 1 should not include "Steak au poivre"
