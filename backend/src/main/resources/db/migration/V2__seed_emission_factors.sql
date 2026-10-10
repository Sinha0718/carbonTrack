-- Baseline emission factors (kg CO2e per unit).
-- Values are teaching approximations of the cited public datasets; verify against the
-- latest official tables before production use. Revisions are added as NEW rows with a
-- later effective_date so historical log entries keep the factor that applied at the time.
-- effective_date 2000-01-01 = baseline that applies to all historical entries.

INSERT INTO emission_factors (category, activity_type, unit, kg_co2e_per_unit, source, effective_date) VALUES
-- Transport (per passenger-km)
('TRANSPORT', 'CAR_PETROL',        'km', 0.170000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - average petrol car', '2000-01-01'),
('TRANSPORT', 'CAR_DIESEL',        'km', 0.171000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - average diesel car', '2000-01-01'),
('TRANSPORT', 'CAR_HYBRID',        'km', 0.120000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - average hybrid car', '2000-01-01'),
('TRANSPORT', 'CAR_ELECTRIC',      'km', 0.047000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - battery EV (grid charging)', '2000-01-01'),
('TRANSPORT', 'MOTORBIKE',         'km', 0.114000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - average motorbike', '2000-01-01'),
('TRANSPORT', 'BUS',               'km', 0.105000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - local bus per passenger-km', '2000-01-01'),
('TRANSPORT', 'TRAIN',             'km', 0.035000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - national rail per passenger-km', '2000-01-01'),
('TRANSPORT', 'METRO',             'km', 0.029000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - underground/metro per passenger-km', '2000-01-01'),
('TRANSPORT', 'FLIGHT_DOMESTIC',   'km', 0.246000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - domestic flight incl. radiative forcing', '2000-01-01'),
('TRANSPORT', 'FLIGHT_SHORT_HAUL', 'km', 0.151000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - short-haul economy incl. radiative forcing', '2000-01-01'),
('TRANSPORT', 'FLIGHT_LONG_HAUL',  'km', 0.148000, 'UK DESNZ/DEFRA GHG Conversion Factors 2023 - long-haul economy incl. radiative forcing', '2000-01-01'),
('TRANSPORT', 'BICYCLE',           'km', 0.000000, 'No direct emissions', '2000-01-01'),
('TRANSPORT', 'WALK',              'km', 0.000000, 'No direct emissions', '2000-01-01'),

-- Electricity (per kWh)
('ELECTRICITY', 'GRID_AVERAGE', 'kWh', 0.373000, 'US EPA eGRID 2022 - US national average output emission rate', '2000-01-01'),
('ELECTRICITY', 'COAL',         'kWh', 0.820000, 'IPCC AR5 WGIII Annex III - lifecycle median', '2000-01-01'),
('ELECTRICITY', 'NATURAL_GAS',  'kWh', 0.490000, 'IPCC AR5 WGIII Annex III - lifecycle median (combined cycle)', '2000-01-01'),
('ELECTRICITY', 'SOLAR',        'kWh', 0.048000, 'IPCC AR5 WGIII Annex III - lifecycle median (utility PV)', '2000-01-01'),
('ELECTRICITY', 'WIND',         'kWh', 0.011000, 'IPCC AR5 WGIII Annex III - lifecycle median (onshore)', '2000-01-01'),
('ELECTRICITY', 'HYDRO',        'kWh', 0.024000, 'IPCC AR5 WGIII Annex III - lifecycle median', '2000-01-01'),
('ELECTRICITY', 'NUCLEAR',      'kWh', 0.012000, 'IPCC AR5 WGIII Annex III - lifecycle median', '2000-01-01'),

-- Food (per serving, ~125 g of the main ingredient)
('FOOD', 'BEEF',       'serving', 7.500000, 'Poore & Nemecek (2018), Science - median per-kg value scaled to a serving', '2000-01-01'),
('FOOD', 'LAMB',       'serving', 5.000000, 'Poore & Nemecek (2018), Science - median per-kg value scaled to a serving', '2000-01-01'),
('FOOD', 'PORK',       'serving', 0.900000, 'Poore & Nemecek (2018), Science - median per-kg value scaled to a serving', '2000-01-01'),
('FOOD', 'CHICKEN',    'serving', 0.750000, 'Poore & Nemecek (2018), Science - median per-kg value scaled to a serving', '2000-01-01'),
('FOOD', 'FISH',       'serving', 0.640000, 'Poore & Nemecek (2018), Science - median per-kg value scaled to a serving', '2000-01-01'),
('FOOD', 'VEGETARIAN', 'serving', 0.500000, 'Poore & Nemecek (2018), Science - estimated mixed vegetarian meal', '2000-01-01'),
('FOOD', 'VEGAN',      'serving', 0.300000, 'Poore & Nemecek (2018), Science - estimated mixed plant-based meal', '2000-01-01'),

-- Shopping (per USD spent)
('SHOPPING', 'CLOTHING',        'USD', 0.260000, 'US EPA USEEIO v2 supply-chain factors (approx.)', '2000-01-01'),
('SHOPPING', 'ELECTRONICS',     'USD', 0.200000, 'US EPA USEEIO v2 supply-chain factors (approx.)', '2000-01-01'),
('SHOPPING', 'FURNITURE',       'USD', 0.320000, 'US EPA USEEIO v2 supply-chain factors (approx.)', '2000-01-01'),
('SHOPPING', 'HOUSEHOLD_GOODS', 'USD', 0.380000, 'US EPA USEEIO v2 supply-chain factors (approx.)', '2000-01-01'),
('SHOPPING', 'BOOKS_AND_MEDIA', 'USD', 0.250000, 'US EPA USEEIO v2 supply-chain factors (approx.)', '2000-01-01'),
('SHOPPING', 'PERSONAL_CARE',   'USD', 0.300000, 'US EPA USEEIO v2 supply-chain factors (approx.)', '2000-01-01'),
('SHOPPING', 'TOYS_AND_SPORTS', 'USD', 0.280000, 'US EPA USEEIO v2 supply-chain factors (approx.)', '2000-01-01');
