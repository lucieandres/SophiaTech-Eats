Feature: Admin Get Statistics

  Scenario: Get Statistics Report from All Restaurants
    Given I am logged in as an admin
    When I request statistics report from all restaurants
    Then I should receive a statistics report

  Scenario: Get Statistics Report from One Restaurant
    Given I am logged in as an admin
    And there is a restaurant with name "Le Gourmet", id 1
    When I request statistics report from restaurant with id 1
    Then I should receive a statistics report

  Scenario: Get Statistics Report from One Restaurant without Login
    Given I not logged in as an admin
    When I request statistics report from restaurant with id 1
    Then I should receive an unauthorized operation message

  Scenario: Get Statistics Report from All Restaurants without Login
    Given I not logged in as an admin
    When I request statistics report from all restaurants
    Then I should receive an unauthorized operation message