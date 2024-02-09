Feature: Admin Create Buffet Order

  Scenario: Successful Creation of a Buffet Order
    Given I am logged in as an university staff
    And there is a customer account with firstname "John", lastname "Doe", and password "customer123"
    And there is a restaurant with the name "Pizzeria Napoli" with id 3
    And there is a menu "Pizza Margherita" with price 10 in the restaurant "Pizzeria Napoli"
    When I attempt to create a buffet order for the recipient customer and 10 items from the restaurant
    Then the buffet order should be created successfully
    And the buffet order should be associated with the recipient customer
    And the buffet order should be associated with the restaurant
    And the restaurant and customer should receive a notification
    And the buffet order don't need to be payed and delivered

  Scenario: Unsuccessful Creation of a Buffet Order
    Given I am not logged in as an university staff
    And there is a customer account with firstname "John", lastname "Doe", and password "customer123"
    And there is a restaurant with the name "Pizzeria Napoli" with id 3
    And there is a menu "Pizza Margherita" with price 10 in the restaurant "Pizzeria Napoli"
    When I attempt to create a buffet order for the recipient customer and 10 items from the restaurant
    Then I should get an error message

  Scenario: Creation BuffetOrder error with too many items for the restaurant
    Given I am logged in as an university staff
    And there is a customer account with firstname "John", lastname "Doe", and password "customer123"
    And there is a restaurant with the name "Pizzeria Napoli" with id 3
    And there is a menu "Pizza Margherita" with price 10 in the restaurant "Pizzeria Napoli"
    When I attempt to create a buffet order for the recipient customer and 200 items from the restaurant
    Then I should get an error message