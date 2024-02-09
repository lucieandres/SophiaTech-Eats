Feature: Restaurant reports

  Scenario: Generating reports for restaurant order volumes
    Given I am logged in as an admin
    When I request statistics report from all restaurants
    Then I should receive a statistics report
